package com.aplikasiq.submitpertama

import FavoritEntity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListFavoritAdapter(private val listFavorit: ArrayList<FavoritEntity>) :
    RecyclerView.Adapter<ListFavoritAdapter.ListViewHolder>() {
    private lateinit var onItemClickCallback: OnItemClickCallback
    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder {
        val view: View =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_favorit, viewGroup, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (id, title, description, date) = listFavorit[position]
        holder.gitID = id
        holder.gitTitle.text= title
        holder.gitDescription.text = description
        holder.gitDate.text = date
        holder.itemView.setOnClickListener {
            onItemClickCallback.onItemClicked(listFavorit[position])

        }

    }

    override fun getItemCount(): Int = listFavorit.size
    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var gitID: Int = 0
        var gitTitle: TextView = itemView.findViewById(R.id.tv_item_title)
        var gitDescription: TextView = itemView.findViewById(R.id.tv_item_description)
        var gitDate: TextView = itemView.findViewById(R.id.tv_item_date)
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: FavoritEntity)
    }
}