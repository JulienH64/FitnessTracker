package com.hollowlog.fitnesstracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var startWorkoutCard: CardView
    private lateinit var exerciseListCard: CardView
    private lateinit var pastWorkoutsCard: CardView
    private lateinit var userProfileButton: ImageView
    private lateinit var routineListCard: CardView

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        // Hide the title bar at the top
        supportActionBar?.hide()

        startWorkoutCard = findViewById(R.id.start_workout_card)
        exerciseListCard = findViewById(R.id.exercise_list_card)
        userProfileButton = findViewById(R.id.user_profile_button)
        pastWorkoutsCard = findViewById(R.id.past_workout_card)
        routineListCard = findViewById(R.id.routine_list_card)

        startWorkoutCard.setOnClickListener { startNewWorkout() }
        exerciseListCard.setOnClickListener { viewExerciseList() }
        userProfileButton.setOnClickListener { viewUserProfile() }
        pastWorkoutsCard.setOnClickListener { pastWorkouts() }
        routineListCard.setOnClickListener { viewRoutineList() }
    }

    // If the user returns back to this screen
    override fun onResume() {

        mAuth = FirebaseAuth.getInstance()

        // Check if there is a user signed in now. If not, back to login screen
        if (mAuth.currentUser == null) {
            val intent = Intent(this@MainActivity, LoginScreen::class.java)
            intent.putExtra("login", "login")
            startActivity(intent)
        }

        super.onResume()
    }

    private fun startNewWorkout() {
        val workoutIntent = Intent(this@MainActivity, StartWorkout::class.java)

        startActivity(workoutIntent)
    }

    private fun viewExerciseList() {
        val exerciseListIntent = Intent(this@MainActivity, MuscleGroupExerciseActivity::class.java)
        exerciseListIntent.putExtra("request", "edit")

        startActivity(exerciseListIntent)
    }

    private fun viewUserProfile() {
        val intent = Intent(this@MainActivity, LoginScreen::class.java)
        startActivity(intent)

    }

    private fun pastWorkouts() {
        val pastIntent = Intent(this@MainActivity, PastWorkouts::class.java)

        startActivity(pastIntent)
    }

    private fun viewRoutineList() {
        val routineIntent = Intent(this@MainActivity, RoutineList::class.java)
        startActivity(routineIntent)
    }
}