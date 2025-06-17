package com.example.handicraft_graduation_project_2025.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handicraft.databinding.FragmentLikesBottomSheetBinding
import com.example.handicraft_graduation_project_2025.ui.adapters.LikeAdapter
import com.example.handicraft.ui.viewmodels.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LikesBottomSheet(private val postId: String) : BottomSheetDialogFragment() {
    private var _binding: FragmentLikesBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLikesBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = LikeAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.getLikes(postId).observe(viewLifecycleOwner) { likes ->
            adapter.submitList(likes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}