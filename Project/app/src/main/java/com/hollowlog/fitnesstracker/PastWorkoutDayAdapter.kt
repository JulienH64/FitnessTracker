package com.hollowlog.fitnesstracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PastWorkoutDayAdapter(private val exerciseList: List<String>,
                            private val exerciseData: DataSnapshot):
    RecyclerView.Adapter<PastWorkoutDayAdapter.ExerciseViewHolder>() {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {

        // Get a reference to the Database and Authenticator
        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        // Inflate view
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.past_exercise, parent, false)

        return ExerciseViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ExerciseViewHolder, position: Int) {
        val text = exerciseList[position]

        // Change layout depending if view is showing an exercise name or set information
        if (text.startsWith("wrw22")) {
            viewHolder.exercise.setPadding(100,0,0,0)
            viewHolder.exercise.textSize = 16.toFloat()
            viewHolder.exercise.text = text.substringAfterLast("wrw22")
        } else {
            viewHolder.exercise.setPadding(0,50,0,0)
            viewHolder.exercise.text = text
        }
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }

    class ExerciseViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        val exercise: TextView = itemView.findViewById(R.id.past_exercise_text)
    }
}