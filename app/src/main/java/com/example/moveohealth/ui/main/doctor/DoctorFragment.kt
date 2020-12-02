package com.example.moveohealth.ui.main.doctor

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moveohealth.R
import com.example.moveohealth.adapters.UserListAdapter
import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import com.example.moveohealth.databinding.FragmentDoctorBinding
import com.example.moveohealth.ui.displayTakeActionDialog
import com.example.moveohealth.ui.main.MainActivity
import com.example.moveohealth.ui.main.MainViewModel
import com.example.moveohealth.ui.main.state.MainStateEvent.RemoveFirstPatientFromQueue
import com.example.moveohealth.util.TopSpacingItemDecoration
import timber.log.Timber


class DoctorFragment: Fragment(R.layout.fragment_doctor) {

    private var _binding: FragmentDoctorBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: UserListAdapter

    val viewModel: MainViewModel by activityViewModels()

    // Paging List configuration to use it with FirestorePagingOptions
//    private var pagingConfig = PagedList.Config.Builder()
//        .setEnablePlaceholders(false)
//        .setPrefetchDistance(2)
//        .setPageSize(10)
//        .build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDoctorBinding.bind(view)
        setClickListeners()
        setupRecyclerView()
        subscribeObservers()
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
        viewModel.setStateEvent(
            RemoveFirstPatientFromQueue(mAdapter.getFirstUserId())
        )
    }

    private fun setupRecyclerView() {
        binding.rv.apply {
            layoutManager = LinearLayoutManager(context)
            val spacingItemDecoration = TopSpacingItemDecoration(
                context.resources.getDimension(R.dimen.margin_recycler_view_item).toInt(),
                1
            )
            addItemDecoration(spacingItemDecoration)
            mAdapter = UserListAdapter().apply {
                onItemClick = {
                    Timber.tag(APP_DEBUG).d("DoctorFragment: onItemClick: user = $it")
                }
            }
            adapter = mAdapter
        }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, { dataState ->
            Timber.tag(APP_DEBUG).e("DoctorFragment: subscribeObservers: dataState = $dataState")
            dataState?.success?.data?.getContentIfNotHandled()?.let { viewState ->
                viewModel.setDoctorFieldsViewState(viewState.doctorFields)
            }
        })
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState?.doctorFields?.waitingList?.let {
                Timber.tag(APP_DEBUG).e("DoctorFragment: subscribeObservers: submitting list of ${it.size}")
                mAdapter.submitList(it)
                (activity as MainActivity).showFab(it.isNotEmpty())
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}