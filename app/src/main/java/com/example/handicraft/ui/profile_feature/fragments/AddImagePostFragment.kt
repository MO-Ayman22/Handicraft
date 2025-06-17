package com.example.handicraft.ui.profile_feature.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.handicraft.R
import com.example.handicraft.data.models.Post
import com.example.handicraft.data.models.User
import com.example.handicraft.databinding.FragmentAddImagePostBinding
import com.example.handicraft.ui.profile_feature.viewmodels.AddPostViewModel
import com.example.handicraft.utils.Resource
import com.example.handicraft_graduation_project_2025.utils.CloudinaryManager
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import kotlinx.coroutines.launch
import java.util.UUID

class AddImagePostFragment : DialogFragment() {

    private var _binding: FragmentAddImagePostBinding? = null
    private val binding get() = _binding!! // Use a backing property for null safety

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private val imageUris = mutableListOf<Uri>()

    private lateinit var viewModel: AddPostViewModel
    private var currentUser: User? = null // Make currentUser nullable to handle initial state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModels()
        initImagePicker()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddImagePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            setBackgroundDrawableResource(android.R.color.transparent)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Call super.onViewCreated

        CloudinaryManager.initialize(requireContext())
        observeUser()
        observePostState()
        setClickListeners()
    }

    // Always nullify the binding in onDestroyView to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Initializes the ViewModel for this fragment.
     */
    private fun initViewModels() {
        viewModel = ViewModelProvider(this)[AddPostViewModel::class.java]
    }

    /**
     * Sets up the ActivityResultLauncher for picking images from the gallery.
     */
    private fun initImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    val clipData = intent.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            clipData.getItemAt(i).uri?.let { imageUris.add(it) }
                        }
                    } else {
                        intent.data?.let { imageUris.add(it) }
                    }
                    updateUI()
                }
            }
        }
    }

    /**
     * Sets up click listeners for various UI elements.
     */
    private fun setClickListeners() {
        binding.pickImageButton.setOnClickListener { openGallery() }
        binding.publishButton.setOnClickListener { validateAndUpload() }
        // Dismiss dialog when clicking outside the post card (root view)
        binding.root.setOnClickListener { dismiss() }
        // Consume click event on postCard to prevent dismissal when clicking inside
        binding.postCard.setOnClickListener { /* Do nothing */ }
    }

    /**
     * Observes the current user from the ViewModel.
     * The user's UID is needed to associate posts.
     */
    private fun observeUser() {
        val userId = SharedPrefUtil.getUid(requireContext())
        if (userId.isNullOrEmpty()) {
            showToast("User not logged in.")
            dismiss()
            return
        }
        viewModel.fetchUserById(userId)
        viewModel.user.observe(viewLifecycleOwner) { user -> // Use viewLifecycleOwner
            currentUser = user
            if (currentUser == null) {
                showToast("Failed to fetch user data.")
                dismiss()
            }
        }
    }

    /**
     * Observes the status of saving a post to provide user feedback.
     */
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

    /**
     * Launches the gallery to pick images.
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        imagePickerLauncher.launch(intent)
    }

    /**
     * Updates the UI to display selected images in a GridLayout.
     */
    private fun updateUI() {
        binding.emptyContainer.visibility = if (imageUris.isEmpty()) View.VISIBLE else View.GONE
        binding.imageContainer.removeAllViews() // Clear previous views

        // Display up to the first 4 selected images
        val imagesToDisplay = imageUris.take(4)
        imagesToDisplay.forEachIndexed { index, uri ->
            val imageViewItem = createImageItemView(index, uri)
            binding.imageContainer.addView(imageViewItem)
        }

        // Handle "+X more" overlay if more than 4 images are selected
        if (imageUris.size > 4) {
            // Get the 4th image item view (at index 3) if it exists
            binding.imageContainer.getChildAt(3)?.apply {
                findViewById<FrameLayout>(R.id.overlay)?.visibility = View.VISIBLE
                findViewById<TextView>(R.id.moreCount)?.apply {
                    text = "+${imageUris.size - 3}"
                    visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Creates and returns a View for displaying an individual image.
     */
    private fun createImageItemView(index: Int, uri: Uri): View {
        val view = layoutInflater.inflate(R.layout.item_image, binding.imageContainer, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val overlay = view.findViewById<FrameLayout>(R.id.overlay)
        val moreCount = view.findViewById<TextView>(R.id.moreCount)
        val deleteButton = view.findViewById<ImageView>(R.id.deleteButton)

        Glide.with(this)
            .load(uri)
            .transform(RoundedCorners(16))
            .into(imageView)

        // Ensure overlay and moreCount are initially hidden as they are handled in updateUI()
        overlay.visibility = View.GONE
        moreCount.visibility = View.GONE

        deleteButton.setOnClickListener {
            onDeleteButtonClicked(index)
        }

        // Apply GridLayout.LayoutParams based on image count and index
        view.layoutParams = GridLayout.LayoutParams().apply {
            width = 0 // Required for weight to work
            height = 0 // Required for weight to work
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, getColSpan(imageUris.size, index), 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, getRowSpan(imageUris.size, index), 1f)
        }

        return view
    }

    /**
     * Handles the deletion of an image from the list.
     */
    private fun onDeleteButtonClicked(index: Int) {
        if (index in 0 until imageUris.size) { // Safe index check
            imageUris.removeAt(index)
            updateUI()
        }
    }

    /**
     * Placeholder for handling clicks on the image overlay.
     */
    private fun onOverlayClicked(index: Int, uri: Uri) {
        // TODO: Implement logic to show a full-screen view of images or navigate to a gallery.
        showToast("Overlay clicked on image at index $index")
    }

    /**
     * Determines the column span for an image in the GridLayout.
     */
    private fun getColSpan(count: Int, index: Int): Int {
        return when (count.coerceAtMost(4)) { // Consider only up to 4 for layout calculation
            1 -> 2 // Single image spans 2 columns
            2 -> 1 // Two images, each 1 column
            3 -> if (index == 0) 2 else 1 // First image spans 2 columns, next two 1 column each
            else -> 1 // Default for 4 (or more) images, each 1 column
        }
    }

    /**
     * Determines the row span for an image in the GridLayout.
     */
    private fun getRowSpan(count: Int, index: Int): Int {
        return if (count.coerceAtMost(4) <= 2) 2 else 1 // If 1 or 2 images, they span 2 rows, otherwise 1 row each
    }

    /**
     * Validates input fields and proceeds with image upload and post creation.
     */
    private fun validateAndUpload() {
        val content = binding.editText.text.toString().trim()
        if (content.isEmpty()) {
            showToast("Please enter post content.")
            return
        }
        if (imageUris.isEmpty()) {
            showToast("Please select at least one image.")
            return
        }
        if (currentUser == null) {
            showToast("User data not loaded. Please try again.")
            return
        }

        uploadImagesAndCreatePost(content)
    }

    /**
     * Uploads selected images to Cloudinary and creates a new post.
     */
    private fun uploadImagesAndCreatePost(content: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.publishButton.isEnabled = false // Disable button during upload

        lifecycleScope.launch {
            try {
                val imageUrls = mutableListOf<String>()
                for (uri in imageUris) {
                    val file = CloudinaryManager.uriToFile(uri, requireContext())
                    val result = CloudinaryManager.uploadImage(
                        context = requireContext(),
                        imageFile = file,
                        collectionPath = "posts", // Better path, e.g., "posts" or "user_uploads"
                        documentId = UUID.randomUUID().toString(),
                        imageField = "imageUrl" // This field is more for internal Cloudinary settings or metadata
                    )
                    if (result.isSuccess) {
                        result.getOrNull()?.let { imageUrls.add(it) }
                    } else {
                        showToast("Image upload failed: ${result.exceptionOrNull()?.message}")
                        binding.progressBar.visibility = View.GONE
                        binding.publishButton.isEnabled = true
                        return@launch // Stop if any image fails to upload
                    }
                }

                if (imageUrls.isEmpty()) {
                    showToast("No images were successfully uploaded.")
                    binding.progressBar.visibility = View.GONE
                    binding.publishButton.isEnabled = true
                    return@launch
                }

                val post = Post(
                    userId = currentUser!!.uid, // currentUser is guaranteed non-null here due to checks
                    content = content,
                    imageUrls = imageUrls,
                    likesCount = 0,
                    commentsCount = 0
                )

                viewModel.savePost(post, currentUser!!.uid)

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.publishButton.isEnabled = true
                showToast("Error during post creation: ${e.message}")
            }
        }
    }

    /**
     * Displays a short toast message.
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}