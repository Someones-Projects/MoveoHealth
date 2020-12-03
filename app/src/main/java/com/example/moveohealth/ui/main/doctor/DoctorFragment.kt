package com.example.moveohealth.ui.main.doctor

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moveohealth.R
import com.example.moveohealth.adapters.UserListAdapter
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.databinding.FragmentBaseMainListBinding
import com.example.moveohealth.model.User
import com.example.moveohealth.ui.displayTakeActionDialog
import com.example.moveohealth.ui.main.BaseMainFragment
import com.example.moveohealth.ui.main.MainActivity
import com.example.moveohealth.ui.main.state.MainStateEvent.EnterNewPatientFromQueue
import com.example.moveohealth.util.TopSpacingItemDecoration
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber


class DoctorFragment: BaseMainFragment(R.layout.fragment_base_main_list) {

    private var _binding: FragmentBaseMainListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBaseMainListBinding.bind(view)
        toolbarInteraction.setToolbarTitle("Hi, Dr ${viewModel.getUsername()}")
        binding.textListHeader.text = "Waiting list:"
        setClickListeners()
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        listenToWaitingListChanges()
    }

    private fun listenToWaitingListChanges() {
        uiCommunicationListener.showProgressBar(show = true)
        jobListener = lifecycleScope.launch {
            Timber.tag(APP_DEBUG).e("DoctorFragment: listenToWaitingListChanges: ...")
            viewModel.listenPatientWaitingListFlow().collect { pairUserAndList ->
                Timber.tag(APP_DEBUG).e("DoctorFragment: collect: pair = $pairUserAndList")
                uiCommunicationListener.showProgressBar(show = false)
                updateUiCurrentPatient(pairUserAndList.first)
                updateUiPatientsList(pairUserAndList.second)
            }
        }
    }

    private fun updateUiCurrentPatient(user: User?) {
        (activity as MainActivity).showFab(user != null)
        binding.textDoctorCurrentPatient.apply {
            if (user != null) {
                visibility = View.VISIBLE
                text = "Your current patient is: ${user.username}"
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun updateUiPatientsList(it: List<User>?) {
        if (it.isNullOrEmpty()) {
            binding.textNoData.text = "No patients waiting"
            binding.textNoData.visibility = View.VISIBLE
            binding.rv.visibility = View.GONE
        } else {
            binding.textNoData.visibility = View.GONE
            binding.rv.visibility = View.VISIBLE
            mAdapter?.submitList(it)
        }
    }

    private fun setClickListeners() {

    }

    fun handleNextPatientClicked() {
        showGetNextPatientDialog()
    }

    private fun showGetNextPatientDialog() {
        activity?.apply {
            val posClickListener = DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
                removePatientFromQueue()
            }
            displayTakeActionDialog(
                title = null,
                body = getString(R.string.alert_get_next_patient),
                posBtnText = getString(R.string.get_next),
                positiveClickListener = posClickListener
            )
        }
    }

    private fun removePatientFromQueue() {
        Timber.tag(APP_DEBUG).e("DoctorFragment: removePatientFromQueue: ")
        mAdapter?.getFirstUserOrNull().let {
            viewModel.setStateEvent(EnterNewPatientFromQueue(it))
        }
    }

    private fun setupRecyclerView() {
        binding.rv.apply {
            layoutManager = LinearLayoutManager(context)
            val spacingItemDecoration = TopSpacingItemDecoration(
                context.resources.getDimension(R.dimen.margin_recycler_view_item).toInt(),
                1
            )
            addItemDecoration(spacingItemDecoration)
            mAdapter = UserListAdapter(patientId = null).apply { // null b/c list of patients
                onClickStart = {
                    Timber.tag(APP_DEBUG).d("DoctorFragment: onItemClick: user = $it")
                }
            }
            adapter = mAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}