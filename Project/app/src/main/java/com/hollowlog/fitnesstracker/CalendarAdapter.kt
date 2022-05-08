package com.hollowlog.fitnesstracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CalendarAdapter(private val daysInMonth: ArrayList<String>,
                               private val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    // Interface to be able to override onClick in PastWorkouts
    interface ItemClickListener {
        fun onListItemClick(position: Int, text: String?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        // Inflate view
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_day, parent, false)

        // Update view heights so dates fit vertically
        val lp = v.layoutParams as GridLayoutManager.LayoutParams
        lp.height = (parent.height / 6)
        v.layoutParams = lp

        // Create viewholder and add onClickListener
        val viewHolder = CalendarViewHolder(v, itemClickListener)
        v.setOnClickListener(viewHolder)

        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: CalendarViewHolder, position: Int) {
        viewHolder.day.text = daysInMonth[position].substringAfterLast("y")

        // Set background color of view to grey if a previous workout occurred on that day
        if (daysInMonth[position].contains('y')) {
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#A6A5A5"))

            // Add tag to indicate that view shows day with past workout
            viewHolder.itemView.tag = "y"
        }
    }

    override fun getItemCount(): Int {
        return daysInMonth.size
    }

    class CalendarViewHolder constructor(itemView: View,
                                         private val itemClickListener: ItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val day: TextView = itemView.findViewById(R.id.calendarDayText)

        // Override onClick with method in PastWorkouts (Guaranteed with interface)
        override fun onClick(view: View) {
            //C heck if selected day has recorded past workouts
            if (view.tag == "y") {
                itemClickListener.onListItemClick(adapterPosition, day.text as String)
            }
        }
    }
}