package com.hollowlog.fitnesstracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Statistics: AppCompatActivity()  {

    companion object {
        private const val TAG = "Statistics"
    }

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var startDate: LocalDate
    private lateinit var endDate: LocalDate
    private lateinit var startDateView: TextView
    private lateinit var endDateView: TextView
    private lateinit var exerciseNameView: Spinner
    private lateinit var pickStartView: ImageView
    private lateinit var pickEndView: ImageView
    private lateinit var statisticsRecyclerView: RecyclerView
    private lateinit var exerciseList: List<String>
    private lateinit var chosenExercise: String
    private lateinit var data: DataSnapshot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics)

        // Hide the title bar at the top
        supportActionBar?.hide()

        // Get a reference to the Database and Authenticator
        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        setDates()

        getDataSnapshot()
    }

    // Gets list of all workouts
    private fun getDataSnapshot() {
        if (mAuth.currentUser != null) {
            mDatabase.child("User").child(mAuth.currentUser!!.uid).get().addOnSuccessListener {

                data = it
                getExerciseList(it)

            }.addOnFailureListener {
                    Log.e(TAG, "Error getting data", it)
            }
        } else {
            Toast.makeText(this, "Error: No user signed in", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Gets list of all exercises for dropdown
    private fun getExerciseList(data: DataSnapshot) {
        exerciseList = listOf()

        data.child("Exercises").children.forEach { muscleGroup ->
            muscleGroup.children.forEach { exercise ->
                exerciseList = exerciseList.plus(exercise.value.toString())
            }
        }
        setExercise()
    }

    // Select exercise using dropdown menu to see its statistics
    private fun setExercise() {

        // Represent dropdown using recyclerview
        exerciseNameView = findViewById(R.id.exercise)
        val adapter = ArrayAdapter(applicationContext, R.layout.spinner, exerciseList)
        exerciseNameView.adapter = adapter

        exerciseNameView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            // Get chosen exercise and display statistics
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                chosenExercise = exerciseList[position]

                displayStats()
            }
        }
    }

    // Show statistics of selected exercise and time period using grid recyclerview
    private fun displayStats() {
        val stats = calculateStats()
        statisticsRecyclerView = findViewById(R.id.statistics_recycler_view)

        val statisticsAdapter = StatisticsAdapter(stats)
        statisticsRecyclerView.layoutManager = GridLayoutManager(applicationContext, 4)
        statisticsRecyclerView.adapter = statisticsAdapter
    }

    // Calculate statistics of selected exercise nad time period
    private fun calculateStats(): List<String> {
        var stats: List<String> = listOf()
        val workouts = data.child("Workouts").children

        // Insert headers for grid
        stats = stats.plus("Date")
        stats = stats.plus("Max\nReps")
        stats = stats.plus("Max\nWeight")
        stats = stats.plus("Total\nWeight")

        workouts.forEach{ workoutDate ->
            val date = LocalDate.parse(workoutDate.key,DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            if (date.isAfter(startDate.minusDays(1)) && date.isBefore(endDate.plusDays(1))) {

                // Keep track of max reps, max weight, and total weight for each day
                var maxReps = 0
                var maxWeight = 0
                var totalWeight = 0

                workoutDate.children.forEach { time ->
                    time.children.forEach { exercise ->
                        if (exercise.child("exerciseName").value?.equals(chosenExercise) == true &&
                            exercise.child("setList").value != null) {

                            exercise.child("setList").children.forEach { set ->
                                val reps = set.child("reps").value.toString().toInt()
                                val weight = set.child("weight").value.toString().toInt()

                                if (reps > maxReps) {
                                    maxReps = reps
                                }

                                if (weight > maxWeight) {
                                    maxWeight = weight
                                }

                                totalWeight += weight * reps
                            }
                        }
                    }
                }
                if (totalWeight != 0) {
                    stats = stats.plus(monthDay(date))
                    stats = stats.plus(maxReps.toString())
                    stats = stats.plus(maxWeight.toString())
                    stats = stats.plus(totalWeight.toString())
                }
            }
        }
        return stats
    }

    // Set date views and attach listeners
    private fun setDates() {
        // Initial end date is the current date and the initial start date is the first
        // of the current month
        endDate = LocalDate.now()
        startDate = endDate.withDayOfMonth(1)

        startDateView = findViewById(R.id.startDate)
        endDateView = findViewById(R.id.endDate)

        // Set view text to formatted selected LocalDate object
        startDateView.text = monthDayYear(startDate)
        endDateView.text = monthDayYear(endDate)

        pickStartView = findViewById(R.id.pick_start_date_image)
        pickEndView = findViewById(R.id.pick_end_date_image)

        pickStartView.setOnClickListener {
            startDate = pickDate()
            startDateView.text = monthDayYear(startDate)
        }

        pickEndView.setOnClickListener {
            endDate = pickDate()
            endDateView.text = monthDayYear(endDate)
        }

    }

    // Choose date using datepicker and return chosen date
    private fun pickDate(): LocalDate {
        val calendar = Calendar.getInstance()

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val datePickerDialog =
            DatePickerDialog(this@Statistics, dateSetListener, year, month,day)

        datePickerDialog.show()

        //Return chosen date as LocalDate object
        return LocalDate.parse("$day $month $year",
            DateTimeFormatter.ofPattern("d M yyyy"))
    }

    //Return date in format of Month Day, Year
    private fun monthDayYear(date: LocalDate?): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        return date!!.format(formatter)
    }

    //Return date in format of Month Day, Year
    private fun monthDay(date: LocalDate?): String {
        val formatter = DateTimeFormatter.ofPattern("MM/dd")
        return date!!.format(formatter)
    }
}