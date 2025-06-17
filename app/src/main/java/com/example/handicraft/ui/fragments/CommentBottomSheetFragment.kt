package com.example.handicraft.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handicraft.databinding.FragmentCommentBottomSheetBinding
import com.example.handicraft.adapters.CommentAdapter
import com.example.handicraft.ui.viewmodels.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomSheetFragment(private val postId: String) : BottomSheetDialogFragment() {
    private var _binding: FragmentCommentBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CommentAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.getComments(postId).observe(viewLifecycleOwner) { comments ->
            adapter.submitList(comments)
        }

        binding.sendButton.setOnClickListener {
            val comment = binding.commentInput.text.toString().trim()
            if (comment.isNotEmpty()) {
                viewModel.addComment(postId, comment)
                binding.commentInput.text?.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}