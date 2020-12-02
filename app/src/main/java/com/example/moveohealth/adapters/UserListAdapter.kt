package com.example.moveohealth.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.example.moveohealth.R
import com.example.moveohealth.model.User
import kotlinx.android.synthetic.main.adapter_user_list_item.view.*

class UserListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((User) -> Unit)? = null

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

    }

    internal inner class WodRecyclerChangeCallback(
        private val adapter: UserListAdapter
    ) : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyDataSetChanged() // notifyDataInserted has bug at the 3 position size
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
        }
    }


    private val differ = AsyncListDiffer(
        WodRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return UserViewHolder(
            itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_user_list_item,
                parent,
                false
            ),
            onItemClicked = onItemClick
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<User>) {
        differ.submitList(list)
    }

    fun getFirstUserId(): String {
        return differ.currentList[0].userId
    }

    class UserViewHolder
    constructor(
        itemView: View,
        private val onItemClicked: ((User) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(user: User) = with(itemView) {

            text_username_user_item_list.text = user.username
            itemView.setOnClickListener {
                onItemClicked?.invoke(user)
            }
        }
    }

}