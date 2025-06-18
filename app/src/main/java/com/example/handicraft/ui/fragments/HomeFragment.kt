package com.example.handicraft.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.handicraft.R
import com.example.handicraft.adapters.BannerAdapter
import com.example.handicraft.adapters.CommentAdapter
import com.example.handicraft.data.models.Post
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FragmentCommentBottomSheetBinding
import com.example.handicraft.databinding.FragmentHomeBinding
import com.example.handicraft.databinding.FragmentLikesBottomSheetBinding
import com.example.handicraft.ui.adapters.PostAdapter
import com.example.handicraft.ui.viewmodels.HomeViewModel
import com.example.handicraft_graduation_project_2025.ui.adapters.LikeAdapter
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class HomeFragment : Fragment(), PostAdapter.OnPostClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var postAdapter: PostAdapter
    private var currentPosts = emptyList<Post>()
    private var usersMap = emptyMap<String,User>()

    private val handler = Handler(Looper.getMainLooper())

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val nextItem = (binding.bannerViewPager.currentItem + 1) % Int.MAX_VALUE
            binding.bannerViewPager.setCurrentItem(nextItem, true)
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupHeaderView()
        setupBanner()
        setupRecyclerView()
        setupListeners()
        setupNavigation()
        observePosts()
        val darkModeMenuItem = binding.navigationView.menu.findItem(R.id.nav_dark_mode)
        val darkModeSwitch= darkModeMenuItem.actionView as? android.widget.Switch
        darkModeSwitch?.apply {
            // Initialize Switch state based on saved preference or current theme
            val isDarkMode = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getBoolean("dark_mode", resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
            isChecked = isDarkMode

            // Set listener for dark mode toggle
            setOnCheckedChangeListener { _, isChecked ->
                val newMode = if (isChecked) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                AppCompatDelegate.setDefaultNightMode(newMode)
                with(requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()) {
                    putBoolean("dark_mode", isChecked)
                    apply()
                }
            }
        }
        viewModel.getAllPosts()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    private fun setupHeaderView() {
        val headerView = binding.navigationView.getHeaderView(0)
        headerView.findViewById<ImageView>(R.id.nav_profile_image)
        headerView.findViewById<TextView>(R.id.nav_user_name)
        binding.profileImage.setOnClickListener {
            with(binding.drawerLayout) {
                if (isDrawerOpen(GravityCompat.START)) closeDrawer(GravityCompat.START)
                else openDrawer(GravityCompat.START)
            }
        }
    }

    private fun setupBanner() {
        binding.bannerViewPager.adapter = BannerAdapter()
        handler.postDelayed(autoScrollRunnable, 3000)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(emptyList(), this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun setupListeners() {
        binding.chatIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chatsFragment)
        }

        binding.notificationIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationFragment)
        }


    }

    private fun setupNavigation() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_show_profile -> findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                R.id.nav_dark_mode -> toggleDarkMode(menuItem.actionView as SwitchCompat)
                R.id.nav_language -> showLanguageDialog()
              //  R.id.nav_favorites -> findNavController().navigate(R.id.action_homeFragment_to_favoritesFragment)
                R.id.nav_sign_out -> showSignOutConfirmation()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val darkModeSwitch = binding.navigationView.menu.findItem(R.id.nav_dark_mode).actionView as SwitchCompat
        darkModeSwitch.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
    }

    private fun toggleDarkMode(switch: SwitchCompat) {
        switch.isChecked = !switch.isChecked
        val mode = if (switch.isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "العربية")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.language)
            .setItems(languages) { _, which ->
                val locale = if (which == 1) Locale("ar") else Locale("en")
                setLocale(locale)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setLocale(locale: Locale) {
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        resources.updateConfiguration(config, resources.displayMetrics)
        requireActivity().recreate()
    }

    private fun showSignOutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.logout) { _, _ ->
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(R.id.action_homeFragment_to_signInFragment2)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observePosts() {
        viewModel.posts.observe(viewLifecycleOwner) {
            currentPosts = it
            viewModel.fetchUsersByIds(it.map { post -> post.userId })
        }
        viewModel.users.observe(viewLifecycleOwner) {
            usersMap = it.associateBy { user -> user.uid }
            postAdapter.updatePosts(currentPosts, usersMap,SharedPrefUtil.getUid(requireContext())!!)
        }
    }

    override fun onCommentClick(position: Int, postId: String) {
        CommentBottomSheet(postId).show(childFragmentManager, "CommentBottomSheet")
    }

    override fun onLikeClick(position: Int, postId: String, isLiked: Boolean) {
        val userId = SharedPrefUtil.getUid(requireContext()) ?: return
        viewModel.toggleLike(postId, userId)
    }

    override fun onLikesCountClick(position: Int, postId: String) {
        LikesBottomSheet(postId).show(childFragmentManager, "LikesBottomSheet")
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoScrollRunnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(autoScrollRunnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(autoScrollRunnable)
        _binding = null
    }
}

class CommentBottomSheet(private val postId: String) : BottomSheetDialogFragment() {
    private var _binding: FragmentCommentBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        viewModel.getComments(postId).observe(viewLifecycleOwner) { adapter.submitList(it) }

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

class LikesBottomSheet(private val postId: String) : BottomSheetDialogFragment() {
    private var _binding: FragmentLikesBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        viewModel.getLikes(postId).observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
