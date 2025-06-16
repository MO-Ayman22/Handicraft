package com.example.handicraft.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentUserIdBinding
import com.example.handicraft.viewmodels.AuthViewModel

class UserIdFragment : Fragment() {
    private var _binding: FragmentUserIdBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Next button
        binding.nextButton.setOnClickListener {
            val nationalId = binding.nationalIdEditText.text.toString().trim()
            if (nationalId.isNotEmpty()) {
                viewModel.updateNationalId(nationalId)
                findNavController().navigate(R.id.action_userIdFragment_to_setYourProfileFragment)
            } else {
                binding.nationalIdEditText.error = getString(R.string.national_id_required)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}