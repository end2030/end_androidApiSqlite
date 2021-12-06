package com.aplikasiq.submitpertama

import Users
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class ListUserAdapter(private val listUser: ArrayList<Users>) :
    RecyclerView.Adapter<ListUserAdapter.ListViewHolder>() {
    private lateinit var onItemClickCallback: OnItemClickCallback
    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder {
        val view: View =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_row_user, viewGroup, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (username, name, imgPhotos, follower, following) = listUser[position]
        holder.gitFollower = follower
        holder.gitFollowing = following
        holder.gitNama.text = name
        holder.gitUsername.text = username
        Glide.with(holder.imgPhotos.context).load(imgPhotos).into(holder.imgPhotos)

        holder.itemView.setOnClickListener {
            onItemClickCallback.onItemClicked(listUser[position])

        }

    }

    override fun getItemCount(): Int = listUser.size
    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgPhotos: ImageView = itemView.findViewById(R.id.img_item_photo)
        var gitNama: TextView = itemView.findViewById(R.id.tv_item_name)
        var gitUsername: TextView = itemView.findViewById(R.id.tv_item_username)
        var gitFollower: String = "Follower"
        var gitFollowing: String = "Following"
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: Users)
    }
}