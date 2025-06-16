package com.example.handicraft.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentCreateNewPasswordBinding
import com.example.handicraft.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class CreateNewPasswordFragment : Fragment() {
    private var _binding: FragmentCreateNewPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateNewPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Submit button
        binding.submitButton.setOnClickListener {
            val newPassword = binding.newPasswordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (validateInputs(newPassword, confirmPassword)) {
               // binding.progressBar.visibility = View.VISIBLE
                viewModel.updatePassword(newPassword)
            }
        }

        // Observe password update result
        viewModel.passwordUpdateResult.observe(viewLifecycleOwner) { result ->
           // binding.progressBar.visibility = View.GONE
            result.onSuccess {
                findNavController().navigate(R.id.action_createNewPasswordFragment_to_signInFragment)
            }.onFailure { e ->
                Snackbar.make(binding.root, e.message ?: getString(R.string.password_update_failed), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun validateInputs(newPassword: String, confirmPassword: String): Boolean {
        return when {
            newPassword.isEmpty() || newPassword.length < 6 -> {
                binding.newPasswordEditText.error = getString(R.string.password_too_short)
                false
            }
            newPassword != confirmPassword -> {
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