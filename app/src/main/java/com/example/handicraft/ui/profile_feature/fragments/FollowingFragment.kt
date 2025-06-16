package com.example.handicraft.ui.profile_feature.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handicraft.R
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FragmentFollowingBinding
import com.example.handicraft.ui.profile_feature.adapters.FollowerItem
import com.example.handicraft.ui.profile_feature.adapters.FollowerRecyclerAdapter
import com.example.handicraft.ui.profile_feature.adapters.OnFollowerItemClickListener
import com.example.handicraft.ui.profile_feature.viewmodels.FollowingViewModel
import com.example.handicraft.utils.Constants
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil

class FollowingFragment : Fragment(), OnFollowerItemClickListener {

    private lateinit var binding: FragmentFollowingBinding
    private lateinit var followingAdapter: FollowerRecyclerAdapter
    private lateinit var followingViewModel: FollowingViewModel
    private lateinit var currentUser: User
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getString(Constants.USER_ID_KEY)
        if (userId == null) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        followingViewModel = ViewModelProvider(this)[FollowingViewModel::class.java]
        initRecyclerView()
        observeViewModel()

        followingViewModel.fetchFollowing(userId!!)
        SharedPrefUtil.getUid(requireContext())?.let { followingViewModel.fetchUserById(it) }
    }

    private fun initRecyclerView() {
        followingAdapter = FollowerRecyclerAdapter(requireContext(), mutableListOf(), this)
        binding.rvFollowing.adapter = followingAdapter
        binding.rvFollowing.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        followingViewModel.user.observe(viewLifecycleOwner) { user ->
            currentUser = user!!
        }
        followingViewModel.users.observe(viewLifecycleOwner) { users ->
            if (users.isNullOrEmpty()) {
                binding.noFollowingLayout.visibility = View.VISIBLE
            } else {
                binding.noFollowingLayout.visibility = View.GONE
            }
            followingAdapter.updateFollowers(users, SharedPrefUtil.getUid(requireContext())!!)
        }
    }

    override fun onFollowButtonClicked(followerItem: FollowerItem) {
        val currentUser = SharedPrefUtil.getUid(requireContext())!!
        val targetUser = followerItem.user.uid

        if (followerItem.isFollowing) {
            followingViewModel.unfollowUser(currentUser, targetUser)
        } else {
            followingViewModel.followUser(currentUser, targetUser)
        }

        followingAdapter.toggleFollowState(followerItem)
    }

    override fun onItemClicked(followerItem: FollowerItem) {
        val fragment = UserProfileFragment.newInstance(followerItem.user.uid, currentUser)
        navigateTo(fragment)
    }

    private fun navigateTo(fragment: Fragment) {
       /* parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()*/
    }

    companion object {
        fun newInstance(userId: String): FollowingFragment {
            return FollowingFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.USER_ID_KEY, userId)
                }
            }
        }
    }
}
