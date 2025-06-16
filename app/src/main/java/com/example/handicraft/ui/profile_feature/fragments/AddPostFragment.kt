package com.example.handicraft_graduation_project_2025.ui.profile_feature.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil

class AddPostFragment : DialogFragment() {
   /* private lateinit var binding: FragmentAddPostBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var postViewModel: PostViewModel
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
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user
            }
        }
        SharedPrefUtil.getUid(requireContext())?.let { userViewModel.fetchUserById(it) }
        setupListeners()
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
                userId = user.userId,
                content = content,
                imageUrl = emptyList(),
                craftType = user.craftType,
            )

            // 🔄 Show loading
            binding.progressBar.visibility = View.VISIBLE
            binding.publishButton.isEnabled = false

            postViewModel.createPost(newPost) { success, errorMsg ->
                // ✅ Hide loading
                binding.progressBar.visibility = View.GONE
                binding.publishButton.isEnabled = true

                if (success) {
                    Toast.makeText(requireContext(), "Post published", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), errorMsg ?: "Failed to publish", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
*/
}
