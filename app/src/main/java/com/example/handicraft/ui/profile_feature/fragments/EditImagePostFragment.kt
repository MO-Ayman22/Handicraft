package com.example.handicraft.ui.profile_feature.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.handicraft.databinding.FragmentEditImagePostBinding
import com.example.handicraft.ui.profile_feature.viewmodels.AddPostViewModel
import com.example.handicraft_graduation_project_2025.utils.CloudinaryManager
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import kotlinx.coroutines.launch
import java.util.UUID


class EditImagePostFragment : DialogFragment() {

    private lateinit var binding: FragmentEditImagePostBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private val imageUris = mutableListOf<Uri>()

    private lateinit var viewModel: AddPostViewModel
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AddPostViewModel::class.java]
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let { intent ->
                        val clipData = intent.clipData
                        if (clipData != null) {
                            for (i in 0 until clipData.itemCount) {
                                clipData.getItemAt(i).uri?.let { uri -> imageUris.add(uri) }
                            }
                        } else {
                            intent.data?.let { uri -> imageUris.add(uri) }
                        }
                        updateUI()
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditImagePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 1),
            (resources.displayMetrics.heightPixels * 1)
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        CloudinaryManager.initialize(requireContext())
        viewModel.user.observe(this) { user ->
            if (user != null) {
                currentUser = user
            }
        }
        viewModel.fetchUserById(SharedPrefUtil.getUid(requireContext())!!)
        binding.pickImageButton.setOnClickListener { openGallery() }
        binding.publishButton.setOnClickListener {
            val content = binding.editText.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter post content", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            uploadImagesAndCreatePost(content)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        imagePickerLauncher.launch(intent)
    }

    private fun updateUI() {
        binding.emptyContainer.visibility = if (imageUris.isNotEmpty()) View.GONE else View.VISIBLE
        binding.imageContainer.removeAllViews()

        val maxImages = imageUris.size.coerceAtMost(4)
        for (i in 0 until maxImages) {
            val view = layoutInflater.inflate(R.layout.item_image, binding.imageContainer, false)
            val imageView = view.findViewById<ImageView>(R.id.imageView)
            val overlay = view.findViewById<FrameLayout>(R.id.overlay)
            val moreCount = view.findViewById<TextView>(R.id.moreCount)

            Glide.with(this).load(imageUris[i]).transform(RoundedCorners(16)).into(imageView)

            if (i == 3 && imageUris.size > 4) {
                overlay.visibility = View.VISIBLE
                moreCount.text = "+${imageUris.size - 3}"
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, getCol(imageUris.size, i), 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, getRow(imageUris.size, i), 1f)
            }

            view.layoutParams = params
            binding.imageContainer.addView(view)
        }
    }

    private fun getCol(count: Int, index: Int): Int {
        return when (count) {
            1 -> 2
            2 -> 1
            3 -> if (index == 0) 2 else 1
            else -> 1
        }
    }

    private fun getRow(count: Int, index: Int): Int {
        return if (count <= 2) 2 else 1
    }

    private fun uploadImagesAndCreatePost(content: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val urls = mutableListOf<String>()
                for (uri in imageUris) {
                    val file = CloudinaryManager.uriToFile(uri, requireContext()) ?: continue
                    val result = CloudinaryManager.uploadImage(
                        context = requireContext(),
                        imageFile = file,
                        collectionPath = "images",
                        documentId = UUID.randomUUID().toString(),
                        imageField = "imageUrl"
                    )
                    if (result.isSuccess) {
                        urls.add(result.getOrNull()!!)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Upload failed: ${result.exceptionOrNull()?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.progressBar.visibility = View.GONE
                        return@launch
                    }
                }

                val post = Post(
                    userId = currentUser.uid,
                    content = content,
                    imageUrls = urls,
                    likesCount = 0,
                    commentsCount = 0
                )

                viewModel.savePost(post, currentUser.uid)

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}



