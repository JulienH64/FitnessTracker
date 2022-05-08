package com.hollowlog.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NewRoutine : AppCompatActivity() {

    private lateinit var nameField: TextView
    private lateinit var saveButton: Button
    private var originalName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_routine)

        saveButton = findViewById(R.id.save_edit_routine_button)
        saveButton.setOnClickListener { saveExercise() }

        nameField = findViewById<TextView>(R.id.edit_routine_name_text_field)
    }

    private fun saveExercise() {
        val newName = nameField.text.toString()

        Log.i(
            "Saving new routine",
            "$newName, $originalName"
        )

        val data = Intent()
        data.putExtra("newName", newName)

        setResult(RESULT_OK, data)
        finish()
    }
}