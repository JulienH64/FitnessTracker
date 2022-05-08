package com.hollowlog.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditRoutine : AppCompatActivity() {
    private lateinit var exerciseTextView: TextView
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var removeExerciseButton: Button
    private lateinit var keepExerciseButton: Button
    private lateinit var exercise: String

    companion object {
        private const val TAG = "EditRoutine"
        private const val REMOVE_EXCERCISE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_routine)

        exercise = intent.getStringExtra("name").toString()

        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        exerciseTextView = findViewById(R.id.routine_exercise_name_text)
        exerciseTextView.text = exercise

        removeExerciseButton = findViewById(R.id.remove_exercise)
        keepExerciseButton = findViewById(R.id.keep_exercise)

        removeExerciseButton.setOnClickListener { removeExercise() }
        keepExerciseButton.setOnClickListener { keepExercise() }
    }

    private fun keepExercise() {
        finish()
    }

    private fun removeExercise() {
        val data = Intent()
        data.putExtra("request", "delete")
        data.putExtra("exercise", exercise)
        setResult(RESULT_OK, data)
        Log.i(TAG, "REACHED REMOVE EXERCISE")
        finish()
    }
}