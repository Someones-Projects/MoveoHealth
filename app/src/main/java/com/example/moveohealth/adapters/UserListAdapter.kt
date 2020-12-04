package com.example.moveohealth.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.example.moveohealth.R
import com.example.moveohealth.model.User
import com.example.moveohealth.model.UserType
import kotlinx.android.synthetic.main.adapter_user_list_item.view.*

class UserListAdapter(
    private val patientId: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onClickStart: ((User) -> Unit)? = null
    var onClickDone: ((User) -> Unit)? = null
    var onClickAddWaiting: ((User) -> Unit)? = null
    var onClickRemoveWaitList: ((User) -> Unit)? = null
    var onClickShowWaiListDialog: ((User) -> Unit)? = null

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
            patientId = patientId,
            onClickStart = onClickStart,
            onClickDone = onClickDone,
            onClickAddWaiting = onClickAddWaiting,
            onClickRemoveWaitList = onClickRemoveWaitList,
            onClickShowWaiListDialog = onClickShowWaiListDialog
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

    fun getFirstUserOrNull(): User? {
        if (differ.currentList.isEmpty()) {
            return null
        }
        return differ.currentList[0]
    }

    class UserViewHolder
    constructor(
        itemView: View,
        private val patientId: String?,
        private val onClickStart: ((User) -> Unit)? = null,
        private val onClickDone: ((User) -> Unit)? = null,
        private val onClickAddWaiting: ((User) -> Unit)? = null,
        private val onClickRemoveWaitList: ((User) -> Unit)? = null,
        private val onClickShowWaiListDialog: ((User) -> Unit)? = null

    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(userItem: User) = with(itemView) {

            text_username_user_item_list.text = userItem.username


            if (userItem.userType == UserType.DOCTOR) {
                var strAction: String = "Start"
                var color: Int = R.color.LimeGreen
                var actionListener: ((User) -> Unit)? = null

                // action button cases
                if (userItem.currentPatient == null) { // doctor available
                    strAction = "Start"
                    color = R.color.LimeGreen
                    actionListener = onClickStart
                } else if (userItem.currentPatient.userId == patientId) { // current patient in session
                    strAction = "Done"
                    color = R.color.Orange
                    actionListener = onClickDone
                } else {
                    val existsInWaitingList = userItem.waitingList?.any{ it.userId == patientId } ?: false
                    if (existsInWaitingList) {
                        strAction = "Cancel wait"
                        color = R.color.IndianRed
                        actionListener = onClickRemoveWaitList
                    } else {
                        strAction = "Add to wait"
                        color = R.color.RoyalBlue
                        actionListener = onClickAddWaiting
                    }
                }

                // apply to button
                button_make_action.apply {
                    visibility = View.VISIBLE
                    text = strAction
                    backgroundTintList = ContextCompat.getColorStateList(context,color)

//                    setBackgroundColor(color)
                    setOnClickListener {
                        actionListener?.invoke(userItem)
                    }
                }

                val available = (userItem.currentPatient == null)

                // available/busy tag
                text_doctor_available_user_item_list.apply {
                    visibility = View.VISIBLE
                    text = if (available) "Available" else "Busy"
                    setTextColor(
                        resources.getColor(
                            if (available) R.color.Green else R.color.DarkRed
                        )
                    )
                }
                // waiting list size display
                text_waiting_user_item_list.apply {
                    visibility = if (available) View.INVISIBLE else View.VISIBLE
                    text = if (available) "" else "${userItem.waitingList?.size ?: 0}"
                    if (!available) {
                        setOnClickListener {
                            onClickShowWaiListDialog?.invoke(userItem)
                        }
                    }
                }
            }// end doctor changing

        }

    }

}