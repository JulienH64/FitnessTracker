package com.hollowlog.fitnesstracker


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.ListFragment

class ExerciseListFragment : ListFragment() {

    private lateinit var mCallback: ListSelectionListener

    interface ListSelectionListener {
        fun onListItemClick(position: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the list adapter for this ListFragment
        listAdapter =
            ArrayAdapter(requireActivity(), R.layout.list_item, listOf<String>())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Make sure that the hosting Activity has implemented
        // the SelectionListener callback interface. We need this
        // because when an item in this ListFragment is selected,
        // the hosting Activity's onItemSelected() method will be called.

        try {
            mCallback = context as ListSelectionListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement SelectionListener")
        }

    }

    // When the user selects an item from the screen, we return it to the FragContainer
    override fun onListItemClick(l: ListView, view: View, position: Int, id: Long) {
        // Notify the hosting Activity that a selection has been made.
        mCallback!!.onListItemClick(position)
    }

    // Loads the selected muscle group exercises into the list on screen
    fun showExerciseList(list: List<String>) {
        Log.i("ExerciseList", "$list")
        listAdapter =
            ArrayAdapter(requireActivity(), R.layout.list_item, list)
    }
}