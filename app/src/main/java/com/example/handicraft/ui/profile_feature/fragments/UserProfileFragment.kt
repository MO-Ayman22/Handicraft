package com.example.handicraft.ui.profile_feature.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide

import com.example.handicraft.R
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FragmentUserProfileBinding
import com.example.handicraft.ui.adapters.OnProfilePostClickListener
import com.example.handicraft.ui.adapters.ProfilePostAdapter
import com.example.handicraft.ui.profile_feature.adapters.OnProfileProductClickListener
import com.example.handicraft.ui.profile_feature.adapters.ProfileProductAdapter
import com.example.handicraft.ui.profile_feature.viewmodels.UserProfileViewModel
import com.example.handicraft.utils.Constants
import com.google.android.material.tabs.TabLayout

class UserProfileFragment : Fragment(), OnProfileProductClickListener, OnProfilePostClickListener {

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var thisUser: User
    private lateinit var currentUser: User
    private lateinit var postsAdapter: ProfilePostAdapter
    private lateinit var productsAdapter: ProfileProductAdapter
    private var userMap: Map<String, User> = emptyMap()
    private var currentProductsList: List<Product> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = arguments?.getParcelable(Constants.USER_KEY) ?: return
        val userId = arguments?.getString(Constants.USER_ID_KEY) ?: return
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]

        setupObservers()
        setupClickListeners()
        userProfileViewModel.fetchUserById(userId)
    }

    private fun setupObservers() {
        userProfileViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                thisUser = it
                initUserInfo()
                initNumericalData()
                setupTabs()
                initProductsRecycler()
                initPostsRecycler()
            }
        }

        userProfileViewModel.userProducts.observe(viewLifecycleOwner) { products ->
            val userIds = products.map { it.userId }.distinct()
            currentProductsList = products
            userProfileViewModel.fetchUsersByIds(userIds)
        }

        userProfileViewModel.users.observe(viewLifecycleOwner) { users ->
            userMap = users.associateBy { it.uid }
            productsAdapter.updateList(currentProductsList, userMap, thisUser)
        }
    }

    private fun setupClickListeners() {
        binding.layoutFollowers.setOnClickListener {
            val fragment = FollowersFragment.newInstance(thisUser.uid)
            navigateTo(fragment)
        }
        binding.layoutFollowing.setOnClickListener {
            val fragment = FollowingFragment.newInstance(thisUser.uid)
            navigateTo(fragment)
        }
        binding.btnFollow.setOnClickListener {
            val currentUser = currentUser.uid
            val targetUser = thisUser.uid

            if (binding.btnFollow.text == requireContext().getString(R.string.unfollow)) {
                userProfileViewModel.unfollowUser(currentUser, targetUser)
                binding.btnFollow.text = requireContext().getString(R.string.follow)
                binding.btnFollow.background =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.follow_but_style)
                binding.btnFollow.setTextColor(requireContext().getColor(R.color.custom_background_color))
                binding.tvProfileFollowersCount.text =
                    (binding.tvProfileFollowersCount.text.toString().toInt() - 1).toString()
            } else {
                userProfileViewModel.followUser(currentUser, targetUser)
                binding.btnFollow.text = requireContext().getString(R.string.unfollow)
                binding.btnFollow.background =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.unfollow_but_style)
                binding.btnFollow.setTextColor(requireContext().getColor(R.color.purple_500))
                binding.tvProfileFollowersCount.text =
                    (binding.tvProfileFollowersCount.text.toString().toInt() + 1).toString()
            }
        }
    }

    private fun initUserInfo() {
        binding.apply {
            if (currentUser.following.contains(thisUser.uid)) {
                binding.btnFollow.text = requireContext().getString(R.string.unfollow)
                binding.btnFollow.background =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.unfollow_but_style)
                binding.btnFollow.setTextColor(requireContext().getColor(R.color.purple_500))
            } else {
                binding.btnFollow.text = requireContext().getString(R.string.follow)
                binding.btnFollow.background =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.follow_but_style)
                binding.btnFollow.setTextColor(requireContext().getColor(R.color.custom_background_color))
            }
            tvProfileUserName.text = thisUser.username
            tvProfileUserEmail.text = thisUser.email
            tvProfileUserAddress.apply {
                text = thisUser.location
                visibility = if (thisUser.location.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            tvArtizen.visibility = if (thisUser.userType == Constants.HANDIMAKER_KEY) View.VISIBLE else View.GONE

            thisUser.profileImageUrl?.takeIf { it.isNotEmpty() }?.let { url ->
                Glide.with(this@UserProfileFragment)
                    .load(url)
                    .placeholder(R.drawable.ic_user)
                    .into(imgProfileUser)
            }
        }
    }

    private fun initNumericalData() {
        binding.apply {
            tvProfileFollowersCount.text = thisUser.followers.size.toString()
            tvProfileFollowingCount.text = thisUser.following.size.toString()
            tvProfilePostsCount.text = thisUser.posts.size.toString()
        }
    }

    private fun initProductsRecycler() {
        productsAdapter = ProfileProductAdapter(emptyList(), this)
        binding.rvProfileProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = productsAdapter
            setHasFixedSize(true)
        }
    }

    private fun initPostsRecycler() {

        postsAdapter = ProfilePostAdapter(emptyList(),this,)
        binding.rvProfilePosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postsAdapter
        }
    }

    private fun setupTabs() {
        binding.profileTabLayout.removeAllTabs()

        if (thisUser.userType == Constants.HANDIMAKER_KEY) {
            binding.profileTabLayout.addTab(binding.profileTabLayout.newTab().setText("Posts"))
            binding.profileTabLayout.addTab(binding.profileTabLayout.newTab().setText("Products"))
        } else {
            binding.profileTabLayout.addTab(binding.profileTabLayout.newTab().setText("Posts"))
        }

        binding.profileTabLayout.getTabAt(0)?.select()

        binding.profileTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.text) {
                    "Posts" -> showPosts()
                    "Products" -> showProducts()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        showPosts() // Default tab content
    }

    private fun showPosts() {
        binding.rvProfilePosts.visibility = View.GONE
        binding.rvProfileProducts.visibility = View.GONE
        binding.tvNoResults.text = getString(R.string.this_user_have_no_posts_yet)
        // TODO: Replace dummy list with real posts
    }

    private fun showProducts() {
        binding.rvProfileProducts.visibility = View.VISIBLE
        binding.rvProfilePosts.visibility = View.GONE
        binding.tvNoResults.text = getString(R.string.this_user_have_no_products_yet)
        userProfileViewModel.fetchProductsByUser(thisUser.uid)
    }

    private fun navigateTo(fragment: Fragment) {
       /* parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()*/
    }

    override fun onProductClick(productId: String, position: Int) {
        // TODO: Navigate to ProductDetailsFragment
    }


    override fun onFavouriteToggle(productId: String, newState: Boolean) {
        if (newState)
            userProfileViewModel.addToFavorites(currentUser.uid, productId)
        else
            userProfileViewModel.removeFromFavorites(currentUser.uid, productId)
        productsAdapter.toggleFavouriteState(productId)
    }

    companion object {
        fun newInstance(userId: String, currentUser: User) = UserProfileFragment().apply {
            arguments = Bundle().apply {
                putString(Constants.USER_ID_KEY, userId)
                putParcelable(Constants.USER_KEY, currentUser)
            }
        }
    }

    override fun onCommentClick(position: Int, postId: String) {
       // TODO("Not yet implemented")
    }

    override fun onLikeClick(position: Int, postId: String, isLiked: Boolean) {
       //  TODO("Not yet implemented")
    }

    override fun onLikesCountClick(position: Int, postId: String) {
       // TODO("Not yet implemented")
    }
}
