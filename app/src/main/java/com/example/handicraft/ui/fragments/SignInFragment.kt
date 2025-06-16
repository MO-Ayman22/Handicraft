package com.example.handicraft.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentSignInBinding
import com.example.handicraft.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Sign-in button
        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val rememberMe = binding.rememberMeCheckbox.isChecked

            if (validateInputs(email, password)) {
               // binding.progressBar.visibility = View.VISIBLE
                viewModel.signIn(requireContext(), email, password, rememberMe)
            }
        }

        // Forgot password
        binding.forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_forgetPasswordFragment)
        }

        // Sign-up text
        binding.signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        // Observe sign-in result
        viewModel.signInResult.observe(viewLifecycleOwner) { result ->
            //binding.progressBar.visibility = View.GONE
            result.onSuccess {
                findNavController().navigate(R.id.action_signInFragment_to_mainFragment)
            }.onFailure { e ->
                Snackbar.make(binding.root, e.message ?: getString(R.string.sign_in_failed), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailEditText.error = getString(R.string.invalid_email)
                false
            }
            password.isEmpty() || password.length < 6 -> {
                binding.passwordEditText.error = getString(R.string.password_too_short)
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