package com.example.handicraft.ui.profile_feature.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*

class AddImagePostFragment : DialogFragment() {

   /* private lateinit var binding: FragmentAddImagePostBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private val imageUris = mutableListOf<Uri>()

    private lateinit var postViewModel: PostViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var currentUser: User

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
        binding = FragmentAddImagePostBinding.inflate(inflater, container, false)
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
        CloudinaryManager.initialize(requireContext())
        observeUser()
        observePostState()

        binding.pickImageButton.setOnClickListener { openGallery() }
        binding.publishButton.setOnClickListener { validateAndUpload() }
    }

    private fun initViewModels() {
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
    }

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

    private fun observeUser() {
        val userId = SharedPrefUtil.getUid(requireContext()) ?: return
        userViewModel.fetchUserById(userId)
        userViewModel.user.observe(this) { user ->
            user?.let { currentUser = it }
        }
    }

    private fun observePostState() {
        postViewModel.postState.onEach { state ->
            binding.progressBar.visibility = if (state is PostState.Loading) View.VISIBLE else View.GONE
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        imagePickerLauncher.launch(intent)
    }

    private fun updateUI() {
        binding.emptyContainer.visibility = if (imageUris.isEmpty()) View.VISIBLE else View.GONE
        binding.imageContainer.removeAllViews()

        val maxImages = imageUris.size.coerceAtMost(4)
        for (i in 0 until maxImages) {
            val imageViewItem = createImageItemView(i)
            binding.imageContainer.addView(imageViewItem)
        }
    }

    private fun createImageItemView(index: Int): View {
        val view = layoutInflater.inflate(R.layout.item_image, binding.imageContainer, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val overlay = view.findViewById<FrameLayout>(R.id.overlay)
        val moreCount = view.findViewById<TextView>(R.id.moreCount)
        val deleteButton = view.findViewById<ImageView>(R.id.deleteButton)

        val uri = imageUris[index]

        Glide.with(this)
            .load(uri)
            .transform(RoundedCorners(16))
            .into(imageView)

        // Show overlay for "+x more"
        if (index == 3 && imageUris.size > 4) {
            overlay.visibility = View.VISIBLE
            moreCount.text = "+${imageUris.size - 3}"
        }

        // Set click listener
        overlay.setOnClickListener {
            onOverlayClicked(index, uri)
        }

        // Set long click listener
        deleteButton.setOnClickListener {
            onDeleteButtonClicked(index, uri)
        }

        view.layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = 0
            columnSpec =
                GridLayout.spec(GridLayout.UNDEFINED, getColSpan(imageUris.size, index), 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, getRowSpan(imageUris.size, index), 1f)
        }

        return view
    }

    private fun onDeleteButtonClicked(index: Int, uri: Uri) {
        imageUris.remove(uri)
        updateUI()
    }

    private fun onOverlayClicked(index: Int, uri: Uri) {

    }


    private fun getColSpan(count: Int, index: Int): Int = when (count) {
        1 -> 2
        2 -> 1
        3 -> if (index == 0) 2 else 1
        else -> 1
    }

    private fun getRowSpan(count: Int, index: Int): Int = if (count <= 2) 2 else 1

    private fun validateAndUpload() {
        val content = binding.editText.text.toString().trim()
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter post content", Toast.LENGTH_SHORT).show()
            return
        }
        uploadImagesAndCreatePost(content)
    }

    private fun uploadImagesAndCreatePost(content: String) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val imageUrls = imageUris.mapNotNull { uri ->
                    val file = uri.toFile(requireContext()) ?: return@mapNotNull null
                    val result = CloudinaryManager.uploadImage(
                        context = requireContext(),
                        imageFile = file,
                        collectionPath = "posts",
                        documentId = UUID.randomUUID().toString(),
                        imageField = "imageUrl"
                    )
                    result.getOrNull() ?: run {
                        showToast("Upload failed: ${result.exceptionOrNull()?.message}")
                        binding.progressBar.visibility = View.GONE
                        return@launch
                    }
                }

                val post = Post(
                    userId = currentUser.userId,
                    content = content,
                    imageUrl = imageUrls,
                    craftType = currentUser.craftType,
                    createdAt = null,
                    likesCount = 0,
                    commentsCount = 0
                )

                postViewModel.createPost(post) { success, error ->
                    binding.progressBar.visibility = View.GONE
                    if (success) {
                        showToast("Post created successfully!")
                        dismiss()
                    } else {
                        showToast(error ?: "Failed to create post.")
                    }
                }

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                showToast("Exception: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    */
    */
}
