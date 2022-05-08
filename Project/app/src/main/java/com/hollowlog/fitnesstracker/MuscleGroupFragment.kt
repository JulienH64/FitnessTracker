package com.hollowlog.fitnesstracker

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.ListFragment

class MuscleGroupFragment : ListFragment() {

    private lateinit var mCallback: SelectionListener

    interface SelectionListener {
        fun onItemSelected(position: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the list adapter for this ListFragment
        listAdapter = ArrayAdapter(requireActivity(), R.layout.list_item, listOf<String>())

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Make sure that the hosting Activity has implemented
        // the SelectionListener callback interface. We need this
        // because when an item in this ListFragment is selected,
        // the hosting Activity's onItemSelected() method will be called.

        try {

            mCallback = context as SelectionListener

        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement SelectionListener")
        }

    }

    override fun onListItemClick(l: ListView, view: View, position: Int, id: Long) {

        // Notify the hosting Activity that a selection has been made.
        mCallback!!.onItemSelected(position)

    }

    fun showMuscleGroupList(muscleGroupList: List<String>) {
        // Set the list adapter for this ListFragment
        listAdapter = ArrayAdapter(requireActivity(), R.layout.list_item, muscleGroupList)
    }
}