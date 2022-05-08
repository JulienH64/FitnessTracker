package com.hollowlog.fitnesstracker

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import java.text.FieldPosition
import java.util.zip.Inflater

class SetsRecyclerViewAdapter(
    private val mSets: List<SetObject>,
    private val mRowLayout: Int,
    private val mContext: Context,
    private var repText: TextView,
    private var weightText: TextView,
    private var restField: TextView,
    private var clearButton: Button,
    private var saveButton: Button,

    ) : RecyclerView.Adapter<SetsRecyclerViewAdapter.ViewHolder>() {

    private var currInt = -1
    private var isSelected = false

    fun getCurrInt(): Int {
        return currInt
    }

    fun getIsSelected(): Boolean {
        return isSelected
    }

    fun resetCurrInt() {
        currInt = -1
    }

    // TODO: Reseting the color is hardcoded. Find a way to fix?
    fun resetIsSelected() {
        isSelected = false
        clearButton.setBackgroundColor(mContext.getColor(R.color.dark_gray))
        clearButton.text = "CLEAR"

        saveButton.setBackgroundColor(mContext.getColor(R.color.dark_gray))
        saveButton.text = "SAVE"
    }

    // Create ViewHolder which holds a View to be displayed
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(mRowLayout, viewGroup, false)
        return ViewHolder(v)
    }

    // Binding: The process of preparing a child view to display data corresponding to a position within the adapter.
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {

        // If the item has been selected, color it gray
        if (currInt == viewHolder.adapterPosition) {
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY)
        } else {
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        // Get the current values of the set
        val reps = mSets[i].reps
        val weight = mSets[i].weight
        val rest = mSets[i].restTime

        // This is what will show up on the screen
        viewHolder.mName.text = "$reps reps, $weight lbs, $rest"

        viewHolder.itemView.setOnClickListener { v ->

            // Unselect the data
            if (currInt == viewHolder.adapterPosition) {
                currInt = -1
                isSelected = false

                repText.text = ""
                weightText.text = ""
                restField.text = ""

                clearButton.setBackgroundColor(mContext.getColor(R.color.dark_gray))
                clearButton.text = "CLEAR"

                saveButton.setBackgroundColor(mContext.getColor(R.color.dark_gray))
                saveButton.text = "SAVE"

                notifyDataSetChanged()

            } else {
                isSelected = true
                currInt = viewHolder.adapterPosition

                clearButton.setBackgroundColor(Color.RED)
                clearButton.text = "DELETE"

                saveButton.setBackgroundColor(mContext.getColor(R.color.green))
                saveButton.text = "UPDATE"

                repText.text = reps.toString()
                weightText.text = weight.toString()
                restField.text = rest.toString()

                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return mSets.size
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        internal val mName: TextView = itemView.findViewById(R.id.text)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            // Display a Toast message indicting the selected item

            Toast.makeText(
                view.context,
                mName.text,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}