package com.hollowlog.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*


class RoutineList : AppCompatActivity() {

    companion object {
        private const val TAG = "RoutineList"
        private const val ADD_EXERCISE = 1
        private const val ADD_ROUTINE = 2
        private const val REMOVE_EXCERCISE = 3
    }

    // Global variables for the RoutineList activity
    private lateinit var dropdownMenu: Spinner
    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var listOfRoutines: List<String>
    private lateinit var currentExerciseList: List<ExerciseObject>
    private lateinit var newRoutineButton: Button
    private lateinit var addExerciseRoutineButton: Button
    private lateinit var deleteRoutineButton: Button
    private lateinit var routine: Any


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.routine_list)

        // Get a reference to the Database
        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser == null) {
            Toast.makeText(this, "Error: No user signed in", Toast.LENGTH_LONG).show()
            finish()
        }

        // Initialize the spinner
        dropdownMenu = findViewById(R.id.routine_dropdown_menu)

        currentExerciseList = listOf()

        // Load the dropdown to allow users to select their routine
        loadDropdownMenu()

        exerciseRecyclerView = findViewById(R.id.routine_current_exercise_recycler_view)

        exerciseRecyclerView.layoutManager = LinearLayoutManager(this)

        newRoutineButton = findViewById(R.id.new_routine)
        addExerciseRoutineButton = findViewById(R.id.add_exercise_routine)
        deleteRoutineButton = findViewById(R.id.delete_routine)

        newRoutineButton.setOnClickListener { newRoutine() }
        addExerciseRoutineButton.setOnClickListener { addExerciseRoutine() }
        deleteRoutineButton.setOnClickListener { deleteRoutine() }
    }

    private fun addExerciseRoutine() {
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(applicationContext, routine.toString(), duration)
        toast.show()
        val intent = Intent(this@RoutineList, MuscleGroupExerciseActivity::class.java)
        intent.putExtra("request", "add")

        startActivityForResult(intent, ADD_EXERCISE)
    }

    private fun deleteRoutine() {
        if (routine.toString() == "Leg Day" || routine.toString() == "Push Day" || routine.toString() == "Pull Day") {
            val text = "Can't delete " + routine.toString() + " because it is a default routine."
            val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
            toast.show()
        } else {
            if (mAuth.currentUser !== null) {
                mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Routines")
                    .child(routine.toString()).removeValue().addOnSuccessListener {
                        val text = "Deleted routine: " + routine.toString() + "."
                        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
                        toast.show()
                        loadDropdownMenu()
                    }
            }
        }
    }

    private fun newRoutine() {
        val intent = Intent(this@RoutineList, NewRoutine::class.java)
        intent.putExtra("request", "add")
        startActivityForResult(intent, ADD_ROUTINE)
    }

    /**
     * Loads the dropdown menu with routine names. Once the user selects a routine to perform,
     * the recycler view is automatically loaded with the exercises associated with that routine.
     */
    private fun loadDropdownMenu() {
        listOfRoutines = listOf()
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
                routine = parent?.getItemAtPosition(position)!!
                mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Routines")
                    .child(routine.toString()).get().addOnSuccessListener {
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
                            RoutineExerciseRecyclerViewAdapter(
                                currentExerciseList,
                                R.layout.list_item,
                                this@RoutineList
                            )
                    }
            }
        }
    }

    private fun updateRecyler() {
        exerciseRecyclerView.adapter =
            RoutineExerciseRecyclerViewAdapter(
                currentExerciseList,
                R.layout.list_item,
                this@RoutineList
            )
    }

    private fun updateDropdown() {
        Collections.sort(listOfRoutines)
        var aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOfRoutines)
        // Loading the dropdown menu portion of the spinner
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Assigning the spinner's adapter to the adapter created above
        dropdownMenu.adapter = aa
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EXERCISE) {
            if (resultCode == RESULT_OK) {
                val exercise = data?.getStringExtra("name")!!
                val muscle = data?.getStringExtra("muscleGroup")!!

                // Add to the database
                mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Routines")
                    .child(routine.toString())
                    .get().addOnSuccessListener {
                        var temp = it.value as List<ExerciseObject>
                        if (currentExerciseList.get(0).exerciseName == "None" && currentExerciseList.size == 1) {
                            currentExerciseList =
                                listOf(ExerciseObject(exercise!!, muscle!!, listOf<SetObject>()))
                            temp = listOf(ExerciseObject(exercise!!, muscle!!, listOf<SetObject>()))
                        } else {
                            currentExerciseList = currentExerciseList.plus(
                                ExerciseObject(
                                    exercise!!,
                                    muscle!!,
                                    listOf<SetObject>()
                                )
                            )
                            temp =
                                temp.plus(ExerciseObject(exercise!!, muscle!!, listOf<SetObject>()))
                        }
                        updateRecyler()
                        mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Routines")
                            .child(routine.toString())
                            .setValue(temp)
                    }
                Toast.makeText(this, "$exercise has been added to $routine", Toast.LENGTH_LONG)
                    .show()
            }
        } else if (requestCode == ADD_ROUTINE) {
            if (resultCode == RESULT_OK) {
                val name = data?.getStringExtra("newName")!!
                mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Routines").get()
                    .addOnSuccessListener {
                        var temp = listOf(ExerciseObject("None", "None", listOf<SetObject>()))
                        listOfRoutines = listOfRoutines.plus(name)
                        mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Routines")
                            .child(name).setValue(temp)
                        updateDropdown()
                    }
                Toast.makeText(this, "$name has been added to routines", Toast.LENGTH_LONG).show()

            }
        } else if (requestCode == REMOVE_EXCERCISE) {
            if (resultCode == RESULT_OK) {
                val exercise = data?.getStringExtra("exercise")!!
                mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Routines")
                    .child(routine.toString())
                    .get().addOnSuccessListener {
                        var temp = it.value as List<ExerciseObject>
                        if (currentExerciseList.size == 1) {
                            currentExerciseList =
                                listOf(ExerciseObject("None", "None", listOf<SetObject>()))
                            temp = listOf(ExerciseObject("None", "None", listOf<SetObject>()))
                        } else {
                            val adapter =
                                exerciseRecyclerView.adapter as RoutineExerciseRecyclerViewAdapter
                            val tempExerciseList = currentExerciseList.toMutableList()
                            tempExerciseList.removeAt(adapter.getCurrPosition())
                            currentExerciseList = tempExerciseList.toList()

                            val tempList = temp.toMutableList()
                            Log.i(TAG, tempList.toString())
                            tempList.removeAt(adapter.getCurrPosition())
                            temp = tempList.toList()
                        }
                        updateRecyler()
                        mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Routines")
                            .child(routine.toString())
                            .setValue(temp)
                    }
                Toast.makeText(this, "Removed $exercise from routine.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
