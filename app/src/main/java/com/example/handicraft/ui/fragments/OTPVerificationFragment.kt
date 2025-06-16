package com.example.handicraft.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentOTPVerificationBinding
import com.example.handicraft.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class OTPVerificationFragment : Fragment() {
    private var _binding: FragmentOTPVerificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOTPVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Verify button
        binding.verifyButton.setOnClickListener {
            val otp = "${binding.otp1.text}${binding.otp2.text}${binding.otp3.text}${binding.otp4.text}"
            if (otp.length == 4) {
               // binding.progressBar.visibility = View.VISIBLE
                viewModel.verifyOtp(requireContext(), otp) // Simplified as email link
            } else {
                Snackbar.make(binding.root, R.string.invalid_otp, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Resend OTP
        binding.textView4.setOnClickListener {
            Snackbar.make(binding.root, R.string.otp_resent, Snackbar.LENGTH_SHORT).show()
        }

        // Observe OTP verification result
        viewModel.otpVerificationResult.observe(viewLifecycleOwner) { result ->
            //binding.progressBar.visibility = View.GONE
            result.onSuccess {
                findNavController().navigate(R.id.action_otpVerificationFragment_to_createNewPasswordFragment)
            }.onFailure { e ->
                Snackbar.make(binding.root, e.message ?: getString(R.string.otp_verification_failed), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}