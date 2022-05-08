package com.hollowlog.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


class PastWorkouts : AppCompatActivity(), CalendarAdapter.ItemClickListener {

    companion object {
        private const val TAG = "PastWorkouts"
    }

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var monthYearTextView: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var chosenDate: LocalDate
    private lateinit var monthsWorkouts: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.past_workouts)

        // Get a reference to the Database and Authenticator
        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        // Get views and listeners for buttons
        getViews()

        // Set the selected date to the current date and show current month
        chosenDate = LocalDate.now()
        retrieveWorkouts()
    }

    // Set views
    private fun getViews() {
        calendarRecyclerView = findViewById(R.id.calendar_recycler_view)
        monthYearTextView = findViewById(R.id.monthYear)
        val prevMonth: Button? = findViewById(R.id.prevMonth)
        val nextMonth: Button? = findViewById(R.id.nextMonth)
        val stats: Button? = findViewById(R.id.statistics)

        prevMonth?.setOnClickListener { prevMonth() }
        nextMonth?.setOnClickListener { nextMonth() }
        stats?.setOnClickListener { statistics() }
    }

    // Display calendar of selected month
    private fun displayMonth() {
        monthYearTextView.text = monthYear(chosenDate)

        val daysInMonth = daysInMonthArray(chosenDate)
        val calendarAdapter = CalendarAdapter(daysInMonth, this)
        calendarRecyclerView.layoutManager = GridLayoutManager(applicationContext, 7)
        calendarRecyclerView.adapter = calendarAdapter
    }

    // Set list of days in month where a workout occurred
    private fun retrieveWorkouts() {
        monthsWorkouts = listOf()

        if (mAuth.currentUser != null) {
            mDatabase.child("User").child(mAuth.currentUser!!.uid)
                .child("Workouts").get().addOnSuccessListener {

                it.children.forEach { value ->
                    monthsWorkouts = monthsWorkouts.plus(value.key.toString())
                }
                Log.i(TAG, "Got value ${it.value}")

                displayMonth()

            }.addOnFailureListener {
                Log.e(TAG, "Error getting data", it)
            }
        } else {
            Toast.makeText(this, "Error: No user signed in", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Returns num array of what days are in the chosen month. Index 0 represents the top left
    // Sunday until Index 37 being the bottommost Tuesday.
    private fun daysInMonthArray(date: LocalDate?): ArrayList<String> {
        val calendarDays = ArrayList<String>()

        // Get the number of days in the chosen month
        val numDays = YearMonth.from(date).lengthOfMonth()

        // Get what day the first of the chosen month is (1 is Monday to 7 is Sunday)
        var firstDayOfWeek = chosenDate.withDayOfMonth(1).dayOfWeek.value

        // Set Sunday to 0 so calendar starts on Index 0
        if (firstDayOfWeek == 7) {firstDayOfWeek = 0}

        // Add days into correct index
        for (i in 1..37) {
            if ((i > firstDayOfWeek) && (i <= numDays + firstDayOfWeek)) {

                //Get date of current loop to compare with dates of previous workouts
                val test= chosenDate.withDayOfMonth(i - firstDayOfWeek).toString()

                //Add "y" in front of day number if a workout happened on that day
                if (monthsWorkouts.contains(test)) {
                    calendarDays.add("y" + (i - firstDayOfWeek).toString())
                } else {
                    calendarDays.add((i - firstDayOfWeek).toString())
                }

            } else {
                calendarDays.add("")
            }
        }
        return calendarDays
    }

    //Display the month and the year at the top
    private fun monthYear(date: LocalDate?): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date!!.format(formatter)
    }

    // Display previous month
    private fun prevMonth() {
        chosenDate.minusMonths(1).also { chosenDate = it }
        retrieveWorkouts()
    }

    // Display next month
    private fun nextMonth() {
        chosenDate.plusMonths(1).also { chosenDate = it }
        retrieveWorkouts()
    }

    // Display statistics
    private fun statistics() {
        val statisticsIntent = Intent(this@PastWorkouts, Statistics::class.java)
        startActivity(statisticsIntent)
    }

    // Start new activity to see a day's workout information if a workout occurred in it
    override fun onListItemClick(position: Int, text: String?) {
        val pastWorkoutDayIntent = Intent(this@PastWorkouts, PastWorkoutDay::class.java)

        val date = text?.toLong()?.let { chosenDate.withDayOfMonth(it.toInt()) }

        pastWorkoutDayIntent.putExtra("Date", date.toString())

        startActivity(pastWorkoutDayIntent)
    }
}
