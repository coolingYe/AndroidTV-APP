package com.zee.setting.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zee.setting.R

class TimerAdapter(private var timers: List<String>) : RecyclerView.Adapter<TimerAdapter.MyHolder>() {

    var setOnClickListener: ((View, String) -> Unit)? = null

    class MyHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val title: TextView = itemView?.findViewById(R.id.tv_timer_plan_title)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_timer_plan, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = timers[position] + "进入关机"
        holder.itemView.setOnClickListener { v ->
            setOnClickListener?.invoke(v, timers[position])
        }
    }

    override fun getItemCount() = timers.size


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(data: List<String>) {
        this.timers = data
        notifyDataSetChanged()
    }

}
