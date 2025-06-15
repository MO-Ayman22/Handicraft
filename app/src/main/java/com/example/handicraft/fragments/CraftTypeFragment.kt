package com.example.handicraft.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentCraftTypeBinding
import com.example.handicraft.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class CraftTypeFragment : Fragment() {
    private var _binding: FragmentCraftTypeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCraftTypeBinding.inflate(inflater, container, false)
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
            val selectedId = binding.craftTypeRadioGroup.checkedRadioButtonId
            val craftType = when (selectedId) {
                R.id.jewelry_radio -> getString(R.string.jewelry)
                R.id.clothing_radio -> getString(R.string.clothing)
                R.id.prints_radio -> getString(R.string.prints)
                R.id.gifts_radio -> getString(R.string.gifts)
                else -> null
            }

            if (craftType != null) {
                viewModel.updateCraftType(craftType)
                findNavController().navigate(R.id.action_craftTypeFragment_to_craftSkillFragment)
            } else {
                Snackbar.make(binding.root, R.string.select_craft_type, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}