package com.hollowlog.fitnesstracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MuscleGroupExerciseActivity : AppCompatActivity(), MuscleGroupFragment.SelectionListener,
    ExerciseListFragment.ListSelectionListener {

    companion object {
        private const val TAG = "MuscleGroupExerciseActivity"
        private const val EDIT_EXERCISE = 0
        private const val ADD_EXERCISE = 1
    }

    private var mMuscleGroupFragment: MuscleGroupFragment? = null
    private var mExerciseListFragment: ExerciseListFragment? = null

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var muscleGroupList: List<String>
    private lateinit var currExerciseList: List<String>
    private var musclePosition = -1
    private var exercisePosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment)

        val fragmentManager = supportFragmentManager
        // Get a reference to the Database and Authenticator
        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()


        if (mAuth.currentUser != null) {
            // Access the value and pass it to the MuscleGroupList Fragment
            mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Muscle Groups").get()
                .addOnSuccessListener {
                    muscleGroupList = it.value as List<String>
                    Log.i(TAG, "Got value ${it.value}")

                    mMuscleGroupFragment = MuscleGroupFragment()

                    // Begin the fragment transaction
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.add(R.id.fragment_container, mMuscleGroupFragment!!)
                    fragmentTransaction.commit()

                    fragmentManager.executePendingTransactions()

                    mMuscleGroupFragment?.showMuscleGroupList(muscleGroupList)

                }.addOnFailureListener {
                    Log.e(TAG, "Error getting data", it)
                }
        } else {
            Toast.makeText(this, "Error: No user signed in", Toast.LENGTH_LONG)
            finish()
        }
    }

    // Creates the menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_only_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // If the user selects one of the options in the menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.add_only_menu_button -> {
                val editIntent =
                    Intent(this@MuscleGroupExerciseActivity, EditExerciseListItem::class.java)

                editIntent.putExtra("request", "add")
                editIntent.putStringArrayListExtra("muscleGroupList", ArrayList(muscleGroupList))

                startActivityForResult(editIntent, ADD_EXERCISE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // When the user selects a muscle group from the MuscleGroupFragment
    override fun onItemSelected(position: Int) {
        Log.i(TAG, "Entered onItemSelected($position)")

        musclePosition = position
        // If in single-pane mode, replace single visible Fragment
        val fragmentManager = supportFragmentManager

        // If there is no ExerciseListFragment instance, then create one
        if (mExerciseListFragment == null)
            mExerciseListFragment = ExerciseListFragment()

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, mExerciseListFragment!!)
            .addToBackStack(null)
        fragmentTransaction.commit()

        fragmentManager.executePendingTransactions()

        // Get the selected muscle group
        val exerciseSelected = muscleGroupList[position]

        // Access the value and pass it to the ExerciseList Fragment
        mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
            .child(exerciseSelected).get().addOnSuccessListener {
            currExerciseList = it.value as List<String>
            Log.i(TAG, "Got value ${it.value}")
            mExerciseListFragment?.showExerciseList(currExerciseList)
        }.addOnFailureListener {
            Log.e(TAG, "Error getting data", it)
        }
    }

    // Used when a user selects an exercise from the ExerciseListFragment
    override fun onListItemClick(position: Int) {

        val selectedExercise = currExerciseList[position]
        val muscleGroup = muscleGroupList[musclePosition]
        exercisePosition = position
        Log.i(TAG, "Selected: $selectedExercise")

        val request = intent.getStringExtra("request")

        // The request came from the workout activity, want to add the exercise
        if (request == "add") {
            val data = Intent()
            data.putExtra("name", selectedExercise)
            data.putExtra("muscleGroup", muscleGroup)

            // Return the selected exercise to be added to the workout
            setResult(RESULT_OK, data)
            finish()

            // Request came from main menu, want to edit the exercise
        } else if (request == "edit") {
            val editIntent =
                Intent(this@MuscleGroupExerciseActivity, EditExerciseListItem::class.java)

            editIntent.putExtra("name", selectedExercise)

            editIntent.putExtra("request", "edit")
            editIntent.putStringArrayListExtra("muscleGroupList", ArrayList(muscleGroupList))
            editIntent.putExtra("muscleGroup", musclePosition)

            startActivityForResult(editIntent, EDIT_EXERCISE)
        }
    }

    /**
     * When the user finishes editing the exercise
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_EXERCISE) {
            if (resultCode == RESULT_OK) {

                // User wants to delete the exercises
                if (data?.getStringExtra("delete") == "delete") {
                    deleteExerciseItem(data)

                    // User hit save button and wants to change data
                } else {
                    editExerciseItem(data)
                }
            }
        } else if (requestCode == ADD_EXERCISE) {
            if (resultCode == RESULT_OK) {
                val name = data?.getStringExtra("newName")
                val muscleGroup = data?.getStringExtra("newMuscleGroup")

                // Add to the database
                mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
                    .child(muscleGroup!!).get().addOnSuccessListener {
                    var temp = it.value as List<String>

                    temp = temp.plus(name!!)

                    temp = temp.sorted()

                    if (musclePosition != -1 && muscleGroup == muscleGroupList[musclePosition] && mExerciseListFragment?.isVisible!!) {
                        currExerciseList = temp

                        mExerciseListFragment?.showExerciseList(currExerciseList)
                    }

                    mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
                        .child(muscleGroup).setValue(temp)
                    Toast.makeText(this, "$name has been added to $muscleGroup", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun deleteExerciseItem(data: Intent?) {
        val name = data!!.getStringExtra("name")
        val muscleGroup = data!!.getStringExtra("muscleGroup")

        Log.i(TAG, "Deleting this exercise: $name, $muscleGroup")

        var newList = listOf<String>()

        // Loop through the current exercise list
        currExerciseList.forEach { value ->
            if (value != name) {
                newList = newList.plus(value)
            }
        }

        // Updated the currently shown exercise list on screen
        currExerciseList = newList
        mExerciseListFragment?.showExerciseList(currExerciseList)

        mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
            .child(muscleGroup!!).setValue(currExerciseList)

        Toast.makeText(this, "$name has been deleted", Toast.LENGTH_LONG).show()
    }

    private fun editExerciseItem(data: Intent?) {
        val newExerciseName = data!!.getStringExtra("newName")
        val newMuscleGroup = data!!.getStringExtra("newMuscleGroup")
        val originalExerciseName = data!!.getStringExtra("originalName")
        val originalMuscleGroup = data!!.getStringExtra("originalMuscleGroup")

        var newList = listOf<String>()

        // User edited the name of the exercise but not the muscle group
        if (newExerciseName != originalExerciseName && newMuscleGroup == originalMuscleGroup) {
            Log.i(TAG, "Name changed: $originalExerciseName -> $newExerciseName")

            // Loop through the current exercise list
            currExerciseList.forEach { value ->

                newList = if (value == originalExerciseName) {
                    newList.plus(newExerciseName.toString())
                } else {
                    newList.plus(value)
                }
            }

            // Update the current exercise fragment
            currExerciseList = newList
            mExerciseListFragment?.showExerciseList(currExerciseList)

            // Update the database with the edited exercise name
            mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
                .child(originalMuscleGroup!!)
                .setValue(currExerciseList)

            Toast.makeText(
                this,
                "$originalExerciseName is now $newExerciseName",
                Toast.LENGTH_LONG
            )
                .show()

            // User changed the muscle group but not the name
        } else if (newExerciseName == originalExerciseName && newMuscleGroup != originalMuscleGroup) {

            // Loop through the current exercise list
            currExerciseList.forEach { value ->
                if (value != originalExerciseName) {
                    newList = newList.plus(value)
                }
            }

            // Updated the currently shown exercise list on screen
            currExerciseList = newList
            mExerciseListFragment?.showExerciseList(currExerciseList)

            // Update the database's original muscle group list with the exercise removed
            mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
                .child(originalMuscleGroup!!)
                .setValue(currExerciseList)

            mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
                .child(newMuscleGroup!!).get()
                .addOnSuccessListener {
                    var muscleList = it.value as List<String>

                    // Add the exercise to the list
                    muscleList = muscleList.plus(originalExerciseName!!)

                    muscleList = muscleList.sorted()

                    // Update the new muscle group list with the added exercise
                    mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
                        .child(newMuscleGroup)
                        .setValue(muscleList)

                    Toast.makeText(
                        this,
                        "$originalExerciseName is now in $newMuscleGroup",
                        Toast.LENGTH_LONG
                    )
                        .show()


                }.addOnFailureListener {
                    Log.e("firebase", "Error getting data", it)
                }

            // User changed both fields
        } else if (newExerciseName != originalExerciseName && newMuscleGroup != originalMuscleGroup) {

            // Loop through the current exercise list
            currExerciseList.forEach { value ->
                if (value != originalExerciseName) {
                    newList = newList.plus(value)
                }
            }

            // Updated the currently shown exercise list on screen
            currExerciseList = newList
            mExerciseListFragment?.showExerciseList(currExerciseList)

            // Update the database's original muscle group list with the exercise removed
            mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
                .child(originalMuscleGroup!!)
                .setValue(currExerciseList)

            // Update the database's original muscle group list with the exercise removed
            mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
                .child(originalMuscleGroup!!)
                .setValue(currExerciseList)


            mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
                .child(newMuscleGroup!!).get()
                .addOnSuccessListener {
                    var muscleList = it.value as List<String>

                    // Add the exercise to the list
                    muscleList = muscleList.plus(newExerciseName!!)

                    muscleList = muscleList.sorted()

                    // Update the new muscle group list with the added exercise
                    mDatabase.child("User").child(mAuth.currentUser!!.uid).child("Exercises")
                        .child(newMuscleGroup)
                        .setValue(muscleList)

                    Toast.makeText(
                        this,
                        "$originalExerciseName is now $newExerciseName, now in $newMuscleGroup",
                        Toast.LENGTH_LONG
                    )
                        .show()

                }.addOnFailureListener {
                    Log.e("firebase", "Error getting data", it)
                }

            // Nothing was edited
        } else {
            Toast.makeText(this, "Nothing was edited", Toast.LENGTH_LONG)
                .show()
        }
    }
}