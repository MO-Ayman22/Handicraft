package com.example.handicraft.ui.fragments


import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.handicraft.data.repository.UserRepository
import com.example.handicraft.databinding.FragmentCommentBottomSheetBinding
import com.example.handicraft.databinding.FragmentHomeBinding
import com.example.handicraft.databinding.FragmentLikesBottomSheetBinding
import com.example.handicraft.ui.adapters.OnPostClickListener
import com.example.handicraft_graduation_project_2025.ui.adapters.LikeAdapter
import com.example.handicraft.ui.adapters.PostAdapter
import com.example.handicraft.ui.viewmodels.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class HomeFragment : Fragment() ,OnPostClickListener{
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var postAdapter: PostAdapter
    private val userRepository = UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
    private val handler = Handler(Looper.getMainLooper())
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            binding.bannerViewPager?.let { viewPager ->
                val nextItem = (viewPager.currentItem + 1) % Int.MAX_VALUE
                viewPager.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 3000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel= ViewModelProvider(this)[HomeViewModel::class.java]
        // Load user data in nav header
        val headerView = binding.navigationView.getHeaderView(0)
        val navProfileImage = headerView.findViewById<ImageView>(R.id.nav_profile_image)
        val navUserName = headerView.findViewById<TextView>(R.id.nav_user_name)

        // Toggle drawer with profile image
        binding.profileImage.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Setup Post Adapter
        postAdapter = PostAdapter(emptyList(), this)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        viewModel.loadPosts()
                    }
                }
            })
        }

        // Navigation clicks
        binding.chatIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chatsFragment)
        }

        binding.notificationIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationFragment)
        }

        binding.bannerViewPager.adapter = BannerAdapter()
        handler.postDelayed(autoScrollRunnable, 3000)

        viewModel.posts.observe(viewLifecycleOwner) {

        }

        // Search functionality
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchPosts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.searchBar.visibility = View.GONE
                    binding.bannerViewPager.visibility = View.GONE
                } else if (dy < 0) {
                    binding.searchBar.visibility = View.VISIBLE
                    binding.bannerViewPager.visibility = View.VISIBLE
                }
            }
        })

        // NavigationView item clicks
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_show_profile -> {
                    findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                }
                R.id.nav_dark_mode -> {
                    val switch = menuItem.actionView as SwitchCompat
                    switch.isChecked = !switch.isChecked
                    if (switch.isChecked) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }
                R.id.nav_favorites -> {
                    // findNavController().navigate(R.id.action_homeFragment_to_favoritesFragment)
                }
                R.id.nav_language -> {
                    showLanguageDialog()
                }
                R.id.nav_settings -> {
                    findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
                }
                R.id.nav_sign_out -> {
                    showSignOutConfirmation()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Initialize Dark Mode switch state
        val darkModeSwitch = binding.navigationView.menu.findItem(R.id.nav_dark_mode).actionView as SwitchCompat
        darkModeSwitch.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "العربية")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.language)
            .setItems(languages) { _, which ->
                val locale = when (which) {
                    0 -> Locale("en")
                    1 -> Locale("ar")
                    else -> Locale("en")
                }
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

    override fun onCommentClick(position: Int, postId: String) {
        CommentBottomSheet(postId).show(childFragmentManager, "CommentBottomSheet")
    }

    override fun onLikeClick(position: Int, postId: String, isLiked: Boolean) {
        viewModel.toggleLike(postId, isLiked)
    }

    override fun onLikesCountClick(position: Int, postId: String) {
        LikesBottomSheet(postId).show(childFragmentManager, "LikesBottomSheet")
    }
}

// CommentBottomSheet and LikesBottomSheet
class CommentBottomSheet(private val postId: String) : BottomSheetDialogFragment() {
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

class LikesBottomSheet(private val postId: String) : BottomSheetDialogFragment() {
    private var _binding: FragmentLikesBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        viewModel.getLikes(postId).observe(viewLifecycleOwner) { likes ->
            adapter.submitList(likes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}