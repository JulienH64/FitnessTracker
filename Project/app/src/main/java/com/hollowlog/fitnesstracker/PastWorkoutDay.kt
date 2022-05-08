package com.hollowlog.fitnesstracker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PastWorkoutDay : AppCompatActivity() {

    companion object {
        private const val TAG = "PastWorkoutDay"
    }

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var monthDayYearTextView: TextView
    private lateinit var timeView: TextView
    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var times: List<String>
    private lateinit var exercises: List<String>
    private lateinit var prevTimeButton: Button
    private lateinit var nextTimeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.past_workout_day)

        // Hide the title bar at the top
        supportActionBar?.hide()

        // Get a reference to the Database and Authenticator
        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        // Get selected date as LocalDate
        val dateString = intent.getStringExtra("Date")
        val date = LocalDate.parse(dateString,DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        setViews(date)

        times = listOf()
        if (mAuth.currentUser != null) {
            if (dateString != null) {
                mDatabase.child("User").child(mAuth.currentUser!!.uid)
                    .child("Workouts").child(dateString).get().addOnSuccessListener {

                        // Get list of workouts that occurred on chosen date
                        it.children.forEach { value ->
                            times = times.plus(value.key.toString())
                        }
                        Log.i(TAG, "Got value ${it.value}")

                        // Display the first workout that occurred on the chosen date
                        displayTime(times, it, 0)

                    }.addOnFailureListener {
                        Log.e(TAG, "Error getting data", it)
                    }
            }
        } else {
            Toast.makeText(this, "Error: No user signed in", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Set views and display the chosen date
    private fun setViews(date: LocalDate) {
        monthDayYearTextView = findViewById(R.id.month_day_year)
        monthDayYearTextView.text = monthDayYear(date)
        exerciseRecyclerView = findViewById(R.id.past_exercise_recycler_view)
    }

    //Return date in format of Month Day, Year
    private fun monthDayYear(date: LocalDate?): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        return date!!.format(formatter)
    }

    // Display the time of the selected workout
    private fun displayTime(times: List<String>, data: DataSnapshot, index: Int) {
        // Get the time of the selected workout
        val time = times[index]
        timeView = findViewById(R.id.time)
        timeView.text = time

        // Get exercises done in selected workout
        exercises = listOf()
        val exerciseData = data.child(time).children

        exerciseData.forEach {exercise ->
            if (exercise.child("setList").value != null) {
                // Add exercise if any sets were done
                exercises = exercises.plus(exercise.child("exerciseName").value.toString())

                val set = exercise.child("setList").children

                set.forEach { currentSet ->
                    // Get reps, weight, and rest for every set
                    val reps = currentSet.child("reps").value
                    val weight = currentSet.child("weight").value
                    val rest = currentSet.child("restTime").value

                    exercises = exercises
                        .plus("wrw22$reps reps, $weight lbs, $rest sec rest")
                }
            }
        }

        // Display the selected workout with a recyclerview
        val exerciseAdapter = PastWorkoutDayAdapter(exercises, data.child(time))
        exerciseRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        exerciseRecyclerView.adapter = exerciseAdapter

        val prevTimeButton = findViewById<Button>(R.id.prevTime)
        val nextTimeButton = findViewById<Button>(R.id.nextTime)

        // Hide previous or next button if there were no previous or following workouts
        if (index == 0) {
            prevTimeButton.visibility = View.INVISIBLE
        } else {
            prevTimeButton.visibility = View.VISIBLE
            prevTimeButton.setOnClickListener { displayTime(times, data, index-1) }
        }

        if (index == (times.size-1)) {
            nextTimeButton.visibility = View.INVISIBLE
        } else {
            nextTimeButton.visibility = View.VISIBLE
            nextTimeButton.setOnClickListener { displayTime(times, data, index+1) }
        }
    }
}
