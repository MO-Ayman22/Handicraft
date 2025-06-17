package com.example.handicraft.ui.profile_feature.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.handicraft.R
import com.example.handicraft.data.models.Post
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FragmentProfileBinding
import com.example.handicraft.ui.adapters.OnProfilePostClickListener
import com.example.handicraft.ui.adapters.ProfilePostAdapter
import com.example.handicraft.ui.fragments.CommentBottomSheet
import com.example.handicraft.ui.fragments.LikesBottomSheet
import com.example.handicraft.ui.profile_feature.adapters.OnProfileProductClickListener
import com.example.handicraft_graduation_project_2025.data.models.Product
import com.example.handicraft.ui.profile_feature.adapters.ProfileProductAdapter
import com.example.handicraft.ui.profile_feature.viewmodels.ProfileViewModel
import com.example.handicraft.utils.Constants
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import com.google.android.material.tabs.TabLayout

class ProfileFragment : Fragment(), OnProfileProductClickListener, OnProfilePostClickListener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var postsAdapter: ProfilePostAdapter
    private lateinit var productsAdapter: ProfileProductAdapter
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userMap: Map<String, User>
    private var currentProductsList: List<Product> = emptyList()
    private var currentPostsList: List<Post> = emptyList()

    private var selectedTab: String = Constants.POSTS_KEY
    private var isFabExpanded = false
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupObservers()
        initRecyclerViews()
        setClickListeners()

        val uid = SharedPrefUtil.getUid(requireContext())
        if (uid != null) {
            profileViewModel.fetchUserById(uid)
        }
    }

    private fun setupObservers() {
        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                currentUser = it
                bindUserInfo(it)
                initNumericalData()
                setupTabs(it.userType)
                // Trigger initial fetch based on the selected tab
                if (selectedTab == Constants.POSTS_KEY) {
                    profileViewModel.fetchPostsByUser(it.uid)
                } else if (selectedTab == Constants.PRODUCTS_KEY) { // Changed to else if to avoid double fetch if tab is not set or defaulted
                    profileViewModel.fetchProductsByUser(it.uid)
                }
            }
        }

        profileViewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            currentPostsList = posts
            val userIds = posts.map { it.userId }.distinct()

            Log.d("ProfileFragment", "User Posts Observed: $posts")
            Log.d("ProfileFragment", "User ids Observed: $userIds")

            if (posts.isEmpty()) {
                binding.rvProfilePosts.visibility = View.GONE
                binding.noResultsLayout.visibility = View.VISIBLE
                binding.tvNoResults.setText(R.string.you_have_no_posts_yet) // Ensure this text is set for posts
            } else {
                binding.rvProfilePosts.visibility = View.VISIBLE
                binding.noResultsLayout.visibility = View.GONE
            }

            // Fetch users for these posts if needed, then update the adapter
            if (userIds.isNotEmpty()) {
                profileViewModel.fetchUsersByIds(userIds)
            } else {
                // If no posts, or no users associated, ensure the adapter is still updated
                // with empty data or existing data to clear previous content.
                if (selectedTab == Constants.POSTS_KEY) { // Only update if "Posts" tab is active
                    postsAdapter.updatePosts(
                        currentPostsList,
                        userMap,
                        currentUser?.uid ?: ""
                    ) // Provide default empty string for uid
                }
            }
        }

        profileViewModel.userProducts.observe(viewLifecycleOwner) { products ->
            currentProductsList = products
            val userIds = products.map { it.userId }.distinct()

            Log.d("ProfileFragment", "User Products Observed: $products")

            if (products.isEmpty()) {
                binding.rvProfileProducts.visibility = View.GONE
                binding.noResultsLayout.visibility = View.VISIBLE
                binding.tvNoResults.setText(R.string.you_have_no_products_yet)
            } else {
                binding.rvProfileProducts.visibility = View.VISIBLE
                binding.noResultsLayout.visibility = View.GONE
            }

            if (userIds.isNotEmpty()) {
                profileViewModel.fetchUsersByIds(userIds)
            } else {
                if (selectedTab == Constants.PRODUCTS_KEY) { // Only update if "Products" tab is active
                    productsAdapter.updateList(
                        currentProductsList,
                        userMap,
                        currentUser!!
                    ) // currentUser should be non-null here, but consider defensive programming
                }
            }
        }

        profileViewModel.users.observe(viewLifecycleOwner) { users ->
            userMap = users.associateBy { it.uid }
            Log.d("ProfileFragment", "Users Map Updated: ${userMap.keys}")

            // Now that userMap is updated, update the relevant adapter if the tab is selected
            if (selectedTab == Constants.PRODUCTS_KEY) {
                productsAdapter.updateList(currentProductsList, userMap, currentUser!!)
            } else { // Implies selectedTab is Constants.POSTS_KEY or initial state
                postsAdapter.updatePosts(currentPostsList, userMap, currentUser?.uid ?: "")
            }
        }
    }

    private fun initNumericalData() {
        binding.apply {
            tvProfileFollowersCount.text = currentUser?.followers?.size.toString()
            tvProfileFollowingCount.text = currentUser?.following?.size.toString()
            tvProfilePostsCount.text = currentUser?.posts?.size.toString()
        }
    }

    private fun setClickListeners() {
        binding.layoutFollowers.setOnClickListener {
            currentUser?.let { navigateTo(FollowersFragment.newInstance(it.uid)) }
        }

        binding.layoutFollowing.setOnClickListener {
            currentUser?.let { navigateTo(FollowingFragment.newInstance(it.uid)) }
        }

        binding.btnEditProfile.setOnClickListener {
            navigateTo(EditProfileFragment())
        }

        binding.fabAddTextPost.setOnClickListener {
            AddPostFragment().show(parentFragmentManager, "AddPostFragment")
        }

        binding.fabAddImagePost.setOnClickListener {
            AddImagePostFragment().show(parentFragmentManager, "AddImagePostFragment")
        }

        binding.fabMainAdd.setOnClickListener {
            if (selectedTab == Constants.POSTS_KEY) {
                toggleFab()
            } else {
                // navigateTo(AddProductFragment())
            }
        }

        binding.viewOverlay.setOnClickListener {
            collapseFab()
        }
    }

    private fun bindUserInfo(user: User) {
        Glide.with(this)
            .load(user.profileImageUrl)
            .placeholder(R.drawable.ic_user)
            .into(binding.imgProfileUser)

        binding.tvProfileUserName.text = user.username
        binding.tvProfileUserEmail.text = user.email
        binding.tvProfileUserAddress.apply {
            text = user.location
            visibility = if (user.location.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        binding.tvArtizen.visibility =
            if (user.userType == Constants.HANDIMAKER_KEY) View.VISIBLE else View.GONE
    }

    private fun initRecyclerViews() {
        productsAdapter = ProfileProductAdapter(emptyList(), this)
        binding.rvProfileProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = productsAdapter
            setHasFixedSize(true)
        }

        postsAdapter = ProfilePostAdapter(emptyList(), this)
        binding.rvProfilePosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postsAdapter
        }
    }

    private fun setupTabs(isHandmaker: String) {
        binding.profileTabLayout.removeAllTabs()

        binding.profileTabLayout.addTab(binding.profileTabLayout.newTab().setText("Posts"))
        if (isHandmaker == Constants.HANDIMAKER_KEY) {
            binding.profileTabLayout.addTab(binding.profileTabLayout.newTab().setText("Products"))
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

        binding.profileTabLayout.post {
            binding.profileTabLayout.getTabAt(0)?.select()
        }
    }

    private fun showPosts() {
        selectedTab = Constants.POSTS_KEY
        binding.rvProfileProducts.visibility = View.GONE
        binding.tvNoResults.setText(R.string.you_have_no_posts_yet)
        currentUser?.let { profileViewModel.fetchPostsByUser(it.uid) }
    }

    private fun showProducts() {
        selectedTab = Constants.PRODUCTS_KEY
        binding.rvProfilePosts.visibility = View.GONE
        binding.tvNoResults.setText(R.string.you_have_no_products_yet)
        currentUser?.let { profileViewModel.fetchProductsByUser(it.uid) }
    }

    private fun toggleFab() {
        if (isFabExpanded) {
            collapseFab()
        } else {
            expandFab()
        }
        isFabExpanded = !isFabExpanded
    }

    private fun expandFab() {
        binding.viewOverlay.visibility = View.VISIBLE
        binding.viewOverlay.animate().alpha(1f).setDuration(300).start()

        binding.layoutSecondaryFabs.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = 100f
            animate().alpha(1f).translationY(0f).setDuration(300).start()
        }
    }

    private fun collapseFab() {
        binding.viewOverlay.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction { binding.viewOverlay.visibility = View.GONE }
            .start()

        binding.layoutSecondaryFabs.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(300)
            .withEndAction { binding.layoutSecondaryFabs.visibility = View.GONE }
            .start()
    }

    private fun navigateTo(fragment: Fragment) {
        /*  parentFragmentManager.beginTransaction()
              .replace(R.id.fragment_container, fragment)
              .addToBackStack(null)
              .commit()*/
    }

    override fun onProductClick(productId: String, position: Int) {
        // TODO: Navigate to ProductDetailsFragment with productId
    }


    override fun onFavouriteToggle(productId: String, newState: Boolean) {
        val currentUser = SharedPrefUtil.getUid(requireContext())!!
        if (newState)
            profileViewModel.addToFavorites(currentUser, productId)
        else
            profileViewModel.removeFromFavorites(currentUser, productId)
        productsAdapter.toggleFavouriteState(productId)
    }

    override fun onCommentClick(position: Int, postId: String) {
        CommentBottomSheet(postId).show(childFragmentManager, "CommentBottomSheet")
    }

    override fun onLikeClick(position: Int, postId: String, isLiked: Boolean) {
        profileViewModel.toggleLike(postId, isLiked)
    }

    override fun onLikesCountClick(position: Int, postId: String) {
        LikesBottomSheet(postId).show(childFragmentManager, "LikesBottomSheet")
    }
}
