package com.example.handicraft.ui.profile_feature.fragments

import android.os.Bundle
import com.example.handicraft.data.models.Post
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FragmentEditPostBinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil

class EditPostFragment : DialogFragment() {
    /*private lateinit var binding: FragmentEditPostBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var postViewModel: PostViewModel
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user
                // init post content
            }
        }
        userViewModel.fetchUserById(SharedPrefUtil.getUid(requireContext())!!)
        setupListeners()
    }

    private fun setupListeners() {
        binding.publishButton.setOnClickListener {
            val content = binding.postEditText.text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter post content", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val user = currentUser
            val newPost = Post(
                userId = user.uid,
                content = content)

            // 🔄 Show loading
            binding.progressBar.visibility = View.VISIBLE
            binding.publishButton.isEnabled = false

            postViewModel.crea(newPost) { success, errorMsg ->
                // ✅ Hide loading
                binding.progressBar.visibility = View.GONE
                binding.publishButton.isEnabled = true

                if (success) {
                    Toast.makeText(requireContext(), "Post published", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        errorMsg ?: "Failed to publish",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }*/

}
