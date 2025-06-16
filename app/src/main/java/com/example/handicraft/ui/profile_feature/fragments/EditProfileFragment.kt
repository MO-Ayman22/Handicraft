package com.example.handicraft.ui.profile_feature.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.handicraft.databinding.FragmentEditProfileBinding
import com.example.handicraft.R
import com.example.handicraft.data.models.User
import com.example.handicraft_graduation_project_2025.ui.profile_feature.FormValidator
import com.example.handicraft_graduation_project_2025.ui.viewmodels.EditProfileViewModel
import com.example.handicraft_graduation_project_2025.utils.CloudinaryManager
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel
    private lateinit var currentUser: User
    private var imageUri: Uri = Uri.EMPTY

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                imageUri = it
                binding.profileImage.setImageURI(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]

        setupObservers()
        setupListeners()
        setupGenderDropdown()

        viewModel.fetchUserById(SharedPrefUtil.getUid(requireContext()) ?: "")
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                currentUser = it
                populateUserData(it)
            }
        }

        viewModel.updateState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                // Optionally navigate back or refresh screen
            }.onFailure {
                Toast.makeText(requireContext(), "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            saveBut.setOnClickListener { updateProfile() }
            edBirthDate.setOnClickListener { showDatePicker() }
            editImageBut.setOnClickListener { openGallery() }
        }
    }

    private fun setupGenderDropdown() {
        val genders = resources.getStringArray(R.array.genders)
        val adapter = ArrayAdapter(requireContext(), R.layout.item_gender, genders)
        binding.edGender.setAdapter(adapter)
    }

    private fun openGallery() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
        galleryLauncher.launch(intent)
    }

    private fun populateUserData(user: User) {
        binding.apply {
            firstName.editText?.setText(user.firstName)
            lastName.editText?.setText(user.lastName)
            email.editText?.setText(user.email)
            phoneNumber.editText?.setText(user.phone.orEmpty())
            userName.editText?.setText(user.username)
            address.editText?.setText(user.location)
            gender.editText?.setText(user.gender)
            birthDate.editText?.setText(user.birthdate)

            Glide.with(requireContext())
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_user)
                .into(profileImage)
        }
    }

    private fun updateProfile() {
        lifecycleScope.launch {
            try {
                val photoUrl = if (imageUri != Uri.EMPTY) {
                    val file = imageUri.toFile()
                    val uploadResult = CloudinaryManager.uploadImage(
                        context = requireContext(),
                        imageFile = file ?: return@launch,
                        collectionPath = "images",
                        documentId = UUID.randomUUID().toString(),
                        imageField = "imageUrl"
                    )

                    if (uploadResult.isSuccess) uploadResult.getOrNull()
                    else {
                        Toast.makeText(
                            requireContext(),
                            "Upload failed: ${uploadResult.exceptionOrNull()?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }
                } else {
                    currentUser.profileImageUrl
                }

                val firstName = binding.firstName.editText?.text.toString().trim()
                val lastName = binding.lastName.editText?.text.toString().trim()
                val email = binding.email.editText?.text.toString().trim()
                val phone = binding.phoneNumber.editText?.text.toString().trim()
                val address = binding.address.editText?.text.toString().trim()
                val gender = binding.edGender.text.toString().trim()
                val birthDate = convertToDate(binding.edBirthDate.text.toString().trim())
                val username = binding.userName.editText?.text.toString().trim()

                FormValidator.validateProfileForm(firstName, lastName, email, phone)?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val updatedUser = User(
                    uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch,
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    email = email,
                    phone = phone,
                    location = address,
                    gender = gender,
                    birthdate = birthDate.toString(),
                    profileImageUrl = photoUrl,
                    userType = currentUser.userType
                )

               // viewModel.updateProfile(updatedUser, email)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.edBirthDate.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun convertToDate(dateStr: String): Date? {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
}
