package com.hollowlog.fitnesstracker

import android.content.DialogInterface
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class EditExerciseListItem : AppCompatActivity() {

    private lateinit var dropdownMenu: Spinner
    private lateinit var nameField: TextView
    private lateinit var saveButton: Button

    private var originalName = ""
    private var originalMuscleGroup = ""
    private var typeRequest = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_exercise)

        saveButton = findViewById(R.id.save_edit_exercise_button)
        saveButton.setOnClickListener { saveExercise() }

        nameField = findViewById<TextView>(R.id.edit_exercise_name_text_field)
        dropdownMenu = findViewById(R.id.edit_exercise_spinner)

        val muscleList = intent.getStringArrayListExtra("muscleGroupList")

        // Create an ArrayAdapter for the spinner to show items
        var aa =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, muscleList!!.toMutableList())

        // Loading the dropdown menu portion of the spinner
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Assigning the spinner's adapter to the adapter created above
        dropdownMenu.adapter = aa

        typeRequest = intent.getStringExtra("request")!!


        if (typeRequest == "edit") {
            editExercise()
        }
    }

    private fun editExercise() {
        // Load the current name for the exercise
        val exerciseName = intent.getStringExtra("name")
        nameField.text = exerciseName
        originalName = exerciseName!!

        // Load the current muscle group for the exercise
        val musclePosition = intent.getIntExtra("muscleGroup", 0)
        dropdownMenu.setSelection(musclePosition)
        originalMuscleGroup = dropdownMenu.selectedItem.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (typeRequest == "edit") {
            menuInflater.inflate(R.menu.delete_only_menu, menu)

            val item = menu?.findItem(R.id.delete_only_menu_button) as MenuItem
            val icon = item.icon as Drawable
            icon.mutate().setColorFilter(resources.getColor(R.color.red), PorterDuff.Mode.MULTIPLY)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.delete_only_menu_button -> {
                deleteExercise()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteExercise() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to delete this exercise?\n\nYou cannot reverse this action")
            .setPositiveButton("Delete",
                DialogInterface.OnClickListener { dialog, id ->
                    val data = Intent()
                    data.putExtra("delete", "delete")
                    data.putExtra("name", originalName)
                    data.putExtra("muscleGroup", originalMuscleGroup)

                    setResult(RESULT_OK, data)
                    finish()
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
        // Create the AlertDialog object and return it
        val dialog = builder.create()
        dialog.setTitle("Delete Exercise")
        dialog.show()
    }

    private fun saveExercise() {
        val newName = nameField.text.toString()
        val newMuscleGroup = dropdownMenu.selectedItem.toString()

        Log.i(
            "Saving new exercise",
            "$newName, $newMuscleGroup, $originalMuscleGroup, $originalName"
        )

        val data = Intent()
        data.putExtra("newName", newName)
        data.putExtra("newMuscleGroup", newMuscleGroup)

        if (typeRequest == "edit") {
            data.putExtra("originalName", originalName)
            data.putExtra("originalMuscleGroup", originalMuscleGroup)
        }

        setResult(RESULT_OK, data)
        finish()
    }
}