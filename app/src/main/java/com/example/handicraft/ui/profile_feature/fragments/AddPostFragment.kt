package com.example.handicraft.ui.profile_feature.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.handicraft.data.models.Post
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FragmentAddPostBinding
import com.example.handicraft.ui.profile_feature.viewmodels.AddPostViewModel
import com.example.handicraft.utils.Resource

import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil

class AddPostFragment : DialogFragment() {
    private lateinit var binding: FragmentAddPostBinding
    private lateinit var viewModel: AddPostViewModel
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AddPostViewModel::class.java]
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user
            }
        }
        SharedPrefUtil.getUid(requireContext())?.let { viewModel.fetchUserById(it) }
        setupListeners()
        observePostState()
    }
    private fun observePostState() {
        viewModel.savePostStatus.observe(viewLifecycleOwner) { result -> // Use viewLifecycleOwner
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.publishButton.isEnabled = false // Disable button during loading
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.publishButton.isEnabled = true
                    showToast("Post created successfully!")
                    dismiss()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.publishButton.isEnabled = true
                    showToast("Failed to create post: ${result.message ?: "Unknown error"}") // Provide more detail
                }
            }
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun setupListeners() {
        binding.newPostFragmentRoot.setOnClickListener {
            dismiss()
        }
        binding.postCard.setOnClickListener {
            // Do nothing to prevent click on the card
        }
        binding.publishButton.setOnClickListener {
            val content = binding.postEditText.text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter post content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val user = currentUser
            val newPost = Post(
                userId = user.uid,
                content = content,
                imageUrls = emptyList()
            )

            // 🔄 Show loading
            binding.progressBar.visibility = View.VISIBLE
            binding.publishButton.isEnabled = false
            viewModel.savePost(newPost, user.uid)
        }
    }
}
