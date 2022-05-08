package com.hollowlog.fitnesstracker

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable
import java.util.ArrayList

class ViewEditExercises : AppCompatActivity() {

    companion object {
        private const val TAG = "ViewEditExercises"
        private const val ADD_SET = 1
        private const val EDIT_SET = 2
    }

    private lateinit var setsRecyclerView: RecyclerView
    private lateinit var setList: List<SetObject>

    private lateinit var repsField: TextView
    private lateinit var weightField: TextView
    private lateinit var restField: TextView

    private lateinit var saveButton: Button
    private lateinit var clearButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout)

        // Update the header text with the name of the exercise
        val exerciseName = intent.getStringExtra("name")
        val exerciseNameHeader = findViewById<TextView>(R.id.exercise_name_text)
        exerciseNameHeader.text = exerciseName

        repsField = findViewById(R.id.reps_text_field)
        weightField = findViewById(R.id.weight_text_field)
        restField = findViewById(R.id.rest_text_field)

        // Buttons and listeners
        saveButton = findViewById(R.id.save_fields_button)
        clearButton = findViewById(R.id.clear_fields_button)

        saveButton.setOnClickListener { saveButtonClicked() }
        clearButton.setOnClickListener { resetFields() }

        setsRecyclerView = findViewById(R.id.exercise_set_recycler)

        setsRecyclerView.layoutManager = LinearLayoutManager(this)

        setList = listOf()

        val bundle = intent.getBundleExtra("bundle") as Bundle
        setList = bundle.getSerializable("setList") as List<SetObject>

        updateAdapter()
    }

    // Override the on back pressed to set our result
    override fun onBackPressed() {
        val intent = Intent()
        val bundle = Bundle()

        bundle.putSerializable("setList", setList as Serializable)
        intent.putExtra("request", "update")
        intent.putExtra("bundle", bundle)

        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }

    private fun saveButtonClicked() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        var repsPerformed = 0
        var weightLifted = 0.0
        var restTime = "No rest"

        if (repsField.text.toString() != "") {
            repsPerformed = repsField.text.toString().toInt()
        }

        if (weightField.text.toString() != "") {
            weightLifted = weightField.text.toString().toDouble()
        }

        if (restField.text.toString() != "") {
            restTime = restField.text.toString()
        }

        // Create the new set object
        val set = SetObject(repsPerformed, weightLifted, restTime)

        val getAdapter = setsRecyclerView.adapter as SetsRecyclerViewAdapter
        val isSelected = getAdapter.getIsSelected()
        val currInt = getAdapter.getCurrInt()

        // Is there currently a position that is selected?
        if (!isSelected) {

            setList = setList.plus(set)

        } else {
            var mutableList = setList.toMutableList()

            mutableList[currInt] = set

            setList = mutableList.toList()
        }

        getAdapter.resetCurrInt()
        getAdapter.resetIsSelected()
        updateAdapter()

        resetFields()
    }

    private fun resetFields() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        val getAdapter = setsRecyclerView.adapter as SetsRecyclerViewAdapter
        val isSelected = getAdapter.getIsSelected()
        val currInt = getAdapter.getCurrInt()

        if (isSelected) {
            var mutableList = setList.toMutableList()
            mutableList.removeAt(currInt)
            setList = mutableList.toList()

            updateAdapter()
        }

        weightField.text = ""
        repsField.text = ""
        restField.text = ""

        getAdapter.resetCurrInt()
        getAdapter.resetIsSelected()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.delete_only_menu, menu)

        val item = menu?.findItem(R.id.delete_only_menu_button) as MenuItem
        val icon = item.icon as Drawable
        icon.mutate().setColorFilter(resources.getColor(R.color.red), PorterDuff.Mode.MULTIPLY)

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
                    data.putExtra("request", "delete")

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

    private fun updateAdapter() {
        setsRecyclerView.adapter = SetsRecyclerViewAdapter(
            setList,
            R.layout.list_item,
            this,
            repsField,
            weightField,
            restField,
            clearButton,
            saveButton,
        )
    }
}