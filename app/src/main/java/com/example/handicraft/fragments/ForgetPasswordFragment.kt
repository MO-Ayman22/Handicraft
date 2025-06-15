package com.example.handicraft.fragments



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentForgetPasswordBinding
import com.example.handicraft.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class ForgetPasswordFragment : Fragment() {
    private var _binding: FragmentForgetPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
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
            val email = binding.emailEditText.text.toString().trim()
            if (validateInput(email)) {
                //binding.progressBar.visibility = View.VISIBLE
                viewModel.resetPassword(email)
            }
        }

        // Observe reset password result
        viewModel.resetPasswordResult.observe(viewLifecycleOwner) { result ->
            //binding.progressBar.visibility = View.GONE
            result.onSuccess {
                findNavController().navigate(R.id.action_forgetPasswordFragment_to_otpVerificationFragment)
            }.onFailure { e ->
                Snackbar.make(binding.root, e.message ?: getString(R.string.reset_password_failed), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun validateInput(email: String): Boolean {
        return if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = getString(R.string.invalid_email)
            false
        } else {
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}