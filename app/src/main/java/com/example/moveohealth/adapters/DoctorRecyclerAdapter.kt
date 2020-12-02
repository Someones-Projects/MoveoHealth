package com.example.moveohealth.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moveohealth.R
import com.example.moveohealth.model.User
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions

class DoctorRecyclerAdapter(
    options: FirestorePagingOptions<User>
) : FirestorePagingAdapter< User,RecyclerView.ViewHolder>(options) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PatientViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_user_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
        TODO("Not yet implemented")
    }

    class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

//        private var authorView: TextView = itemView.findViewById(R.id.post_AuthorName)
//        private var messageView: TextView = itemView.findViewById(R.id.post_Message)
//
//        fun bind(user: User) {
//            authorView.text = post.authorName
//            messageView.text = post.message
//        }
    }

}