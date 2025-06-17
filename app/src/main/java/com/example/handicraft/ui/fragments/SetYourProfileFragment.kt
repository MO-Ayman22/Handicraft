package com.example.handicraft.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentSetYourProfileBinding
import com.example.handicraft.ui.profile_feature.viewmodels.UserProfileViewModel
import com.example.handicraft.viewmodels.AuthViewModel
import com.example.handicraft_graduation_project_2025.utils.CloudinaryManager
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SetYourProfileFragment : Fragment() {
    private var _binding: FragmentSetYourProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel
    private lateinit var imageFile: File
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Display image
                Glide.with(this).load(uri).into(binding.profileImage)

                // Save image to file
                imageFile = CloudinaryManager.uriToFile(uri,requireContext())

                // Upload image to Cloudinary
                uploadImageToCloudinary()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetYourProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Setup gender spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.genderSpinner.adapter = adapter
        }

        // Setup date picker for birthdate
        binding.birthdateEditText.setOnClickListener {
            showDatePicker()
        }

        // Back button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Edit image
        binding.editImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImage.launch(intent)
        }

        // Submit button
        binding.submitButton.setOnClickListener {
            val uid= SharedPrefUtil.getUid(requireContext())!!
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()
            val location = binding.locationEditText.text.toString().trim()
            val gender = binding.genderSpinner.selectedItem.toString()
            val birthdate = binding.birthdateEditText.text.toString().trim()

            if (validateInputs(firstName, lastName, phone, location, birthdate)) {
                //binding.progressBar.visibility = View.VISIBLE
                viewModel.updateProfile(requireContext(),uid, firstName, lastName, phone, location, gender, birthdate)
            }
        }

        // Observe profile update result
        viewModel.profileUpdateResult.observe(viewLifecycleOwner) { result ->
            //binding.progressBar.visibility = View.GONE
            result.onSuccess {
                findNavController().navigate(R.id.action_setYourProfileFragment_to_mainFragment)
            }.onFailure { e ->
                Snackbar.make(binding.root, e.message ?: getString(R.string.profile_update_failed), Snackbar.LENGTH_LONG).show()
            }
        }
    }


    private fun validateInputs(firstName: String, lastName: String, phone: String, location: String, birthdate: String): Boolean {
        return when {
            firstName.isEmpty() -> {
                binding.firstNameEditText.error = getString(R.string.first_name_required)
                false
            }
            lastName.isEmpty() -> {
                binding.lastNameEditText.error = getString(R.string.last_name_required)
                false
            }
            phone.isEmpty() || !phone.matches(Regex("\\d{10,15}")) -> {
                binding.phoneEditText.error = getString(R.string.invalid_phone)
                false
            }
            location.isEmpty() -> {
                binding.locationEditText.error = getString(R.string.location_required)
                false
            }
            birthdate.isEmpty() -> {
                binding.birthdateEditText.error = getString(R.string.birthdate_required)
                false
            }
            else -> {
                // Validate birthdate format and ensure it's not in the future
                try {
                    val date = dateFormat.parse(birthdate)
                    if (date == null || date.after(Calendar.getInstance().time)) {
                        binding.birthdateEditText.error = getString(R.string.invalid_birthdate)
                        false
                    } else {
                        true
                    }
                } catch (e: Exception) {
                    binding.birthdateEditText.error = getString(R.string.invalid_birthdate)
                    false
                }
            }
        }
    }

    private fun uploadImageToCloudinary() {
        imageFile.let { file ->
            CoroutineScope(Dispatchers.Main).launch {
                //binding.progressBar.visibility = View.VISIBLE
                if (!viewModel.isLoggedIn(requireContext())) {
                   // binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, getString(R.string.no_authenticated_user), Snackbar.LENGTH_LONG).show()
                    return@launch
                }

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                   // binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, getString(R.string.no_authenticated_user), Snackbar.LENGTH_LONG).show()
                    return@launch
                }

                val result = CloudinaryManager.uploadImage(
                    context = requireContext(),
                    imageFile = file,
                    collectionPath = "users",
                    documentId = userId,
                    imageField = "profileImageUrl"
                )

               // binding.progressBar.visibility = View.GONE
                result.onSuccess { imageUrl ->
                    Log.d("TAG", "uploadImageToCloudinary: ")
                    viewModel.updateProfileImage(imageUrl)
                }.onFailure { e ->
                    Snackbar.make(binding.root, e.message ?: getString(R.string.image_upload_failed), Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.birthdateEditText.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}