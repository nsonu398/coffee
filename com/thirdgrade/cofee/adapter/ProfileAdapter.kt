package com.thirdgrade.cofee.adapter


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thirdgrade.cofee.R
import com.thirdgrade.cofee.model.UserProfile

class ProfileAdapter(private val onItemClick: (UserProfile) -> Unit) :
    ListAdapter<UserProfile, ProfileAdapter.ProfileViewHolder>(ProfileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProfileViewHolder(
        itemView: View,
        private val onItemClick: (UserProfile) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val imgProfilePic: ImageView = itemView.findViewById(R.id.imgProfilePic)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvBio: TextView = itemView.findViewById(R.id.tvBio)

        fun bind(profile: UserProfile) {
            tvName.text = profile.name
            tvBio.text = profile.bio

            // Load profile image if available
            profile.profilePicUri?.let { uri ->
                imgProfilePic.setImageURI(Uri.parse(uri))
            } ?: run {
                imgProfilePic.setImageResource(R.drawable.ic_person)
            }

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(profile)
            }
        }
    }

    class ProfileDiffCallback : DiffUtil.ItemCallback<UserProfile>() {
        override fun areItemsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem == newItem
        }
    }
}