package com.hollowlog.fitnesstracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatisticsAdapter(private val statisticsList: List<String>) :
    RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsViewHolder {
        // Inflate view
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.statistic_data, parent, false)

        // Edit view height
        //v.layoutParams.height = (parent.height * 0.10).toInt()

        return StatisticsViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: StatisticsViewHolder, position: Int) {
        viewHolder.statistic.text = statisticsList[position]
    }

    override fun getItemCount(): Int {
        return statisticsList.size
    }

    class StatisticsViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statistic: TextView = itemView.findViewById(R.id.statistics_text)
    }
}