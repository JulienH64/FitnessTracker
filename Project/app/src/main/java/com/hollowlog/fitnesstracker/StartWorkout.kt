package com.hollowlog.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class StartWorkout : AppCompatActivity() {

    companion object {

        private const val TAG = "StartWorkout"
        private const val ADD_EXERCISE = 1
        private const val EDIT_EXERCISE = 2
    }

    // Global variables for the StartWorkout activity
    private lateinit var dropdownMenu: Spinner
    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var listOfRoutines: List<String>
    private lateinit var currentExerciseList: List<ExerciseObject>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_new_workout)

        // Get a reference to the Database
        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser == null) {
            Toast.makeText(this, "Error: No user signed in", Toast.LENGTH_LONG).show()
            finish()
        }

        // Initialize the spinner
        dropdownMenu = findViewById(R.id.workout_dropdown_menu)

        listOfRoutines = listOf()
        currentExerciseList = listOf()

        // Load the dropdown to allow users to select their routine
        loadDropdownMenu()

        exerciseRecyclerView = findViewById(R.id.current_exercise_recycler_view)

        exerciseRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.add_menu_item_button -> {
                addNewExercise()
                true
            }

            R.id.save_exercise_menu_button -> {
                finishWorkout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Loads the dropdown menu with routine names. Once the user selects a routine to perform,
     * the recycler view is automatically loaded with the exercises associated with that routine.
     */
    private fun loadDropdownMenu() {

        mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Routines").get()
            .addOnSuccessListener {

                it.children.forEach { value ->
                    listOfRoutines = listOfRoutines.plus(value.key.toString())
                }

                // Create an ArrayAdapter for the spinner to show items
                var aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOfRoutines)

                // Loading the dropdown menu portion of the spinner
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                // Assigning the spinner's adapter to the adapter created above
                dropdownMenu.adapter = aa
            }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }

        // Create the listener for when the user selects an item in the spinner
        dropdownMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = parent?.getItemAtPosition(position)
                if (mAuth.currentUser !== null) {
                    mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Routines")
                        .child(item.toString()).get().addOnSuccessListener {
                        var tempArray = it.value as List<Object>
                        currentExerciseList = listOf<ExerciseObject>()

                        tempArray.forEach {
                            var tempHash = it as HashMap<String, Object>

                            var name = tempHash["exerciseName"] as String
                            var type = tempHash["exerciseType"] as String

                            currentExerciseList = currentExerciseList.plus(
                                ExerciseObject(
                                    name,
                                    type,
                                    listOf<SetObject>()
                                )
                            )
                        }

                        exerciseRecyclerView.adapter =
                            ExerciseRecyclerViewAdapter(
                                currentExerciseList,
                                R.layout.list_item,
                                this@StartWorkout
                            )
                    }
                }

            }
        }
    }

    /**
     * Adds a new exercise to the Exercise RecyclerView
     */
    private fun addNewExercise() {
        val intent = Intent(this@StartWorkout, MuscleGroupExerciseActivity::class.java)
        intent.putExtra("request", "add")

        startActivityForResult(intent, ADD_EXERCISE)
    }

    /**
     * Finish the workout and save it to the overall list of workouts
     */
    private fun finishWorkout() {

        val timeNow = LocalDateTime.now()

        val dateFormat = DateTimeFormatter.ofPattern("yyy-MM-dd")
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

        // Our main key
        val date = timeNow.format(dateFormat)

        // Our sub key
        val time = timeNow.format(timeFormat)

        mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Workouts").child(date)
            .child(time).setValue(currentExerciseList)

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EXERCISE) {
            if (resultCode == RESULT_OK) {
                val exercise = data?.getStringExtra("name")!!
                val muscle = data?.getStringExtra("muscleGroup")!!
                currentExerciseList =
                    currentExerciseList.plus(ExerciseObject(exercise, muscle, listOf<SetObject>()))
                exerciseRecyclerView.adapter =
                    ExerciseRecyclerViewAdapter(currentExerciseList, R.layout.list_item, this)

                Toast.makeText(this, "${exercise.toString()} added to workout", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (requestCode == EDIT_EXERCISE) {
            if (resultCode == RESULT_OK) {

                val request = data?.getStringExtra("request")

                if (request == "update") {
                    val adapter = exerciseRecyclerView.adapter as ExerciseRecyclerViewAdapter
                    val tempList = currentExerciseList.toMutableList()
                    val exercise = tempList[adapter.getCurrPosition()]
                    var setList = listOf<SetObject>()

                    val bundle = data?.getBundleExtra("bundle") as Bundle
                    setList = bundle.getSerializable("setList") as List<SetObject>

                    exercise.setList = setList

                    tempList[adapter.getCurrPosition()] = exercise

                    currentExerciseList = tempList.toList()

                    adapter.resetCurrPosition()
                } else if (request == "delete") {
                    val adapter = exerciseRecyclerView.adapter as ExerciseRecyclerViewAdapter
                    val tempList = currentExerciseList.toMutableList()
                    val exerciseName = tempList[adapter.getCurrPosition()].exerciseName
                    tempList.removeAt(adapter.getCurrPosition())

                    currentExerciseList = tempList.toList()

                    exerciseRecyclerView.adapter =
                        ExerciseRecyclerViewAdapter(currentExerciseList, R.layout.list_item, this)

                    Toast.makeText(
                        this,
                        "${exerciseName.toString()} removed from workout",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }
}