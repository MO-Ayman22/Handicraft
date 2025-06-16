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
import com.example.handicraft.databinding.FragmentFollowersBinding
import com.example.handicraft.ui.profile_feature.adapters.FollowerItem
import com.example.handicraft.ui.profile_feature.adapters.FollowerRecyclerAdapter
import com.example.handicraft.ui.profile_feature.adapters.OnFollowerItemClickListener
import com.example.handicraft.ui.profile_feature.viewmodels.FollowersViewModel
import com.example.handicraft.utils.Constants
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil

class FollowersFragment : Fragment(), OnFollowerItemClickListener {

    private var _binding: FragmentFollowersBinding? = null
    private val binding get() = _binding!!

    private lateinit var followersViewModel: FollowersViewModel
    private lateinit var followersAdapter: FollowerRecyclerAdapter

    private var currentUser: User? = null
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFollowersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getString(Constants.USER_ID_KEY)
        if (userId == null) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        followersViewModel = ViewModelProvider(this)[FollowersViewModel::class.java]

        initRecyclerView()
        observeViewModel()

        followersViewModel.fetchFollowers(userId!!)
        SharedPrefUtil.getUid(requireContext())?.let { followersViewModel.fetchUserById(it) }
    }

    private fun initRecyclerView() {
        followersAdapter = FollowerRecyclerAdapter(requireContext(), mutableListOf(), this)
        binding.rvFollowers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = followersAdapter
        }
    }

    private fun observeViewModel() {
        followersViewModel.user.observe(viewLifecycleOwner) { user ->
            currentUser = user
        }

        followersViewModel.users.observe(viewLifecycleOwner) { followers ->
            if (followers.isNullOrEmpty()) {
                binding.noFollowingLayout.visibility = View.VISIBLE
            } else {
                binding.noFollowingLayout.visibility = View.GONE
                SharedPrefUtil.getUid(requireContext())?.let { uid ->
                    followersAdapter.updateFollowers(followers, uid)
                }
            }
        }
    }

    override fun onFollowButtonClicked(followerItem: FollowerItem) {
        val currentUserId = SharedPrefUtil.getUid(requireContext()) ?: return
        val targetUserId = followerItem.user.uid

        if (followerItem.isFollowing) {
            followersViewModel.unfollowUser(currentUserId, targetUserId)
        } else {
            followersViewModel.followUser(currentUserId, targetUserId)
        }

        followersAdapter.toggleFollowState(followerItem)
    }

    override fun onItemClicked(followerItem: FollowerItem) {
        val user = currentUser ?: return
        val fragment = UserProfileFragment.newInstance(followerItem.user.uid, user)
        navigateTo(fragment)
    }

    private fun navigateTo(fragment: Fragment) {
        /*parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(userId: String): FollowersFragment {
            return FollowersFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.USER_ID_KEY, userId)
                }
            }
        }
    }
}
