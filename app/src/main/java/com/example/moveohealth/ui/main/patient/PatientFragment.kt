package com.example.moveohealth.ui.main.patient

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moveohealth.R
import com.example.moveohealth.adapters.UserListAdapter
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.databinding.FragmentBaseMainListBinding
import com.example.moveohealth.model.User
import com.example.moveohealth.ui.main.BaseMainFragment
import com.example.moveohealth.ui.main.state.MainStateEvent.*
import com.example.moveohealth.util.TopSpacingItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class PatientFragment : BaseMainFragment(R.layout.fragment_base_main_list) {

    private var _binding: FragmentBaseMainListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBaseMainListBinding.bind(view)
        toolbarInteraction.setToolbarTitle("Hi, ${viewModel.getUsername()}")
        binding.textListHeader.text = "Doctors:"
        setClickListeners()
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        listenToDoctorListChanges()
    }

    private fun listenToDoctorListChanges() {
        uiCommunicationListener.showProgressBar(show = true)
        viewModel.doctorList.observe(viewLifecycleOwner, { doctors ->
            Timber.tag(APP_DEBUG).e("PatientFragment: observe: doctors = $doctors")
            doctors?.let { updateUiWithDoctors(it) }
        })
    }

    private fun updateUiWithDoctors(it: List<User>) {
        uiCommunicationListener.showProgressBar(show = false)
        mAdapter?.submitList(it)
        if (it.isEmpty()) {
            binding.iconSortDoctors.visibility = View.INVISIBLE
            binding.textNoData.text = "No Doctors to show"
            binding.textNoData.visibility = View.VISIBLE
            binding.rv.visibility = View.GONE
        } else {
            binding.iconSortDoctors.visibility = View.VISIBLE
            binding.textNoData.visibility = View.GONE
            binding.rv.visibility = View.VISIBLE
        }
    }

    private fun setClickListeners() {
        binding.iconSortDoctors.setOnClickListener {
            viewModel.toggleSort()
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
            mAdapter = UserListAdapter(
                patientId = viewModel.getUserId()
            ).apply {
                onClickStart = { doctor ->
                    onClickStartTreatment(doctor)
                }
                onClickDone = { doctor ->
                    onClickDoneTreatment(doctor)
                }
                onClickAddWaiting = { doctor ->
                    onClickAddToWaitList(doctor)
                }
                onClickRemoveWaitList = { doctor ->
                    onClickRemoveFromWaitList(doctor)
                }
                onClickShowWaiListDialog = { doctor ->
                    showDialogWaitListPatients(doctor.waitingList)
                }
            }
            adapter = mAdapter
        }
    }

    private fun showDialogWaitListPatients(waitingPatients: List<User>?) {
        waitingPatients?.let { users ->
            val items = users.map { it.username }.toTypedArray()
            activity?.apply {
                MaterialAlertDialogBuilder(this)
                    .setTitle(resources.getString(R.string.waiting_list))
                    .setItems(items) { dialog, which ->
                        // Respond to item chosen
                    }
                    .show()
            }
        }
    }

    private fun onClickRemoveFromWaitList(doctor: User) {
        Timber.tag(APP_DEBUG).e("PatientFragment: onClickDone: doctor = $doctor")
        viewModel.setStateEvent(PatientClickedRemoveFromWaitList(doctor.userId))
    }

    private fun onClickAddToWaitList(doctor: User) {
        Timber.tag(APP_DEBUG).e("PatientFragment: onClickAddWaiting: doctor = $doctor")
        viewModel.setStateEvent(PatientClickedAddToWaitList(doctor.userId))
    }

    private fun onClickStartTreatment(doctor: User) {
        Timber.tag(APP_DEBUG).d("PatientFragment: onClickStart: doctor = $doctor")
        viewModel.setStateEvent(PatientClickedStartTreatment(doctor.userId))
    }

    private fun onClickDoneTreatment(doctor: User) {
        Timber.tag(APP_DEBUG).e("PatientFragment: onClickDone: doctor = $doctor")
        viewModel.setStateEvent(
            PatientClickedDoneTreatment(
                doctorId = doctor.userId,
                nextPatient = doctor.waitingList?.firstOrNull()
            )
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mAdapter = null
    }


}