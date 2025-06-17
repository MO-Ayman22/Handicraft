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
import com.example.handicraft.ui.profile_feature.viewmodels.AddPostViewModel
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil

class EditPostFragment : DialogFragment() {
    private lateinit var binding: FragmentEditPostBinding
    private lateinit var viewModel: AddPostViewModel
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
        viewModel = ViewModelProvider(this)[AddPostViewModel::class.java]
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user
                // init post content
            }
        }
        viewModel.fetchUserById(SharedPrefUtil.getUid(requireContext())!!)
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
                content = content
            )


            viewModel.savePost(newPost, user.uid)

        }
    }

}
