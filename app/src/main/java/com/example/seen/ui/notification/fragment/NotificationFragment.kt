package com.example.seen.ui.notification.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seen.databinding.FragmentAccountAndSettingsBinding
import com.example.seen.databinding.FragmentNotificationBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.local.SeenDatabase.Companion.invoke
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.NotificationRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.home.adapter.HomeAdapter
import com.example.seen.ui.home.viewmodel.HomeViewModel
import com.example.seen.ui.home.viewmodel.HomeViewModelProviderFactory
import com.example.seen.ui.notification.adapter.NotificationAdapter
import com.example.seen.ui.notification.viewmodel.NotificationViewModel
import com.example.seen.ui.notification.viewmodel.NotificationViewModelProviderFactory


class NotificationFragment : Fragment() {
    var _binding: FragmentNotificationBinding? = null
    val binding get() = _binding!!

    private var token : String? = null
    lateinit var sharedPref : SharedPreferences

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var viewModel: NotificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getToken()
        initializeViewModel()
        setupRecyclerView()
    }

    private fun getToken() {
        sharedPref = requireActivity().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        token = "Bearer " + sharedPref.getString("token", null)
    }

    private fun initializeViewModel(){
        val notificationRepository = NotificationRepository()

        // create factory
        val factory = NotificationViewModelProviderFactory(
            requireActivity().application,
            notificationRepository,
        )

        // initialize ViewModel
        viewModel = ViewModelProvider(this, factory)
            .get(NotificationViewModel::class.java)
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(requireContext())

        binding.rvHome.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}