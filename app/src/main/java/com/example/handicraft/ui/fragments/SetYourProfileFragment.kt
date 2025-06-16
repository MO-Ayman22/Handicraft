package com.example.handicraft.fragments


import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentSetYourProfileBinding
import com.example.handicraft.viewmodels.AuthViewModel
import com.example.handicraft_graduation_project_2025.utils.CloudinaryManager
import com.google.android.material.snackbar.Snackbar
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
    private val viewModel: AuthViewModel by viewModels()
    private var imageFile: File? = null
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Display image
                Glide.with(this).load(uri).into(binding.profileImage)

                // Save image to file
                imageFile = createTempFile("profile_image", ".jpg")
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(imageFile).use { output ->
                        input.copyTo(output)
                    }
                }

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
        binding.birthdateEditText.setOnClickListener { showDatePicker() }
        binding.birthdateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePicker()
        }
        // Enable clicking the calendar icon (drawableEnd)
        binding.birthdateEditText.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawable = binding.birthdateEditText.compoundDrawablesRelative[2] // drawableEnd
                if (drawable != null && event.rawX >= (binding.birthdateEditText.right - drawable.bounds.width())) {
                    showDatePicker()
                    true
                } else {
                    false
                }
            } else {
                false
            }
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
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()
            val location = binding.locationEditText.text.toString().trim()
            val gender = binding.genderSpinner.selectedItem.toString()
            val birthdate = binding.birthdateEditText.text.toString().trim()

            if (validateInputs(firstName, lastName, phone, location, birthdate)) {
                //binding.progressBar.visibility = View.VISIBLE
                viewModel.updateProfile(requireContext(), firstName, lastName, phone, location, gender, birthdate)
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

    private fun showDatePicker() {
        // Initialize with current date or previously selected date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Update calendar with selected date
                calendar.set(selectedYear, selectedMonth, selectedDay)
                // Format and set date in EditText
                binding.birthdateEditText.setText(dateFormat.format(calendar.time))
            },
            year,
            month,
            day
        )

        // Set maximum date to today (prevent future dates)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
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
        imageFile?.let { file ->
            CoroutineScope(Dispatchers.Main).launch {
                //binding.progressBar.visibility = View.VISIBLE
                val userId = viewModel.isLoggedIn(requireContext()).takeIf { it }?.let {
                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                } ?: run {
                    //binding.progressBar.visibility = View.GONE
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

                //binding.progressBar.visibility = View.GONE
                result.onSuccess { imageUrl ->
                    viewModel.updateProfileImage(requireContext(), imageUrl)
                }.onFailure { e ->
                    Snackbar.make(binding.root, e.message ?: getString(R.string.image_upload_failed), Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}