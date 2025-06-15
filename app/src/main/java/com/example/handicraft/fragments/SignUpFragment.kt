package com.example.handicraft.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentSignUpBinding
import com.example.handicraft.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup user type spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.user_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.userTypeSpinner.adapter = adapter
        }

        // Back button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Sign-up button
        binding.signUpButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            val userType = binding.userTypeSpinner.selectedItem.toString()

            if (validateInputs(username, email, password, confirmPassword)) {
               // binding.progressBar.visibility = View.VISIBLE
                viewModel.signUp(requireContext(), email, password, username, userType)
            }
        }

        // Sign-in text
        binding.signInText.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        // Observe sign-up result
        viewModel.signUpResult.observe(viewLifecycleOwner) { result ->
          //  binding.progressBar.visibility = View.GONE
            val userType = binding.userTypeSpinner.selectedItem.toString()
            result.onSuccess {
                if(userType=="Handimaker")
                findNavController().navigate(R.id.action_signUpFragment_to_craftTypeFragment)
                else
                    findNavController().navigate(R.id.action_signUpFragment_to_setYourProfileFragment)
            }.onFailure { e ->
                Snackbar.make(binding.root, e.message ?: getString(R.string.sign_up_failed), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.usernameEditText.error = getString(R.string.username_required)
                false
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailEditText.error = getString(R.string.invalid_email)
                false
            }
            password.isEmpty() || password.length < 6 -> {
                binding.passwordEditText.error = getString(R.string.password_too_short)
                false
            }
            password != confirmPassword -> {
                binding.confirmPasswordEditText.error = getString(R.string.passwords_dont_match)
                false
            }
            else -> true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}