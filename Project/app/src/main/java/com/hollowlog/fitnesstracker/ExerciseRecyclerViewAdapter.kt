package com.hollowlog.fitnesstracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import java.io.Serializable

internal class ExerciseRecyclerViewAdapter(
    private val mExercises: List<ExerciseObject>,
    private val mRowLayout: Int,
    private val mContext: Context
) : RecyclerView.Adapter<ExerciseRecyclerViewAdapter.ViewHolder>() {

    private var currPosition = -1

    fun getCurrPosition(): Int {
        return currPosition
    }

    fun resetCurrPosition() {
        currPosition = -1
    }

    // Create ViewHolder which holds a View to be displayed
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(mRowLayout, viewGroup, false)
        return ViewHolder(v)
    }

    // Binding: The process of preparing a child view to display data corresponding to a position within the adapter.
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.mName.text = mExercises[i].exerciseName

        // Will bring us to viewing the sets of the selected exercises
        viewHolder.itemView.setOnClickListener { v ->
            currPosition = viewHolder.adapterPosition
            val intent = Intent(v.context, ViewEditExercises::class.java)
            val bundle = Bundle()
            intent.putExtra("name", viewHolder.mName.text)

            bundle.putSerializable("setList", mExercises[i].setList!! as Serializable)
            intent.putExtra("bundle", bundle)

            startActivityForResult(mContext as Activity, intent, 2, bundle)
        }
    }

    override fun getItemCount(): Int {
        return mExercises.size
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

