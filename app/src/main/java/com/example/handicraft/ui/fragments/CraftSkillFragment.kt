package com.example.handicraft.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentCraftSkillBinding
import com.example.handicraft.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class CraftSkillFragment : Fragment() {
    private var _binding: FragmentCraftSkillBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCraftSkillBinding.inflate(inflater, container, false)
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
            val selectedId = binding.craftSkillRadioGroup.checkedRadioButtonId
            val craftSkill = when (selectedId) {
                R.id.beginner_radio -> getString(R.string.beginner)
                R.id.intermediate_radio -> getString(R.string.intermediate)
                R.id.advanced_radio -> getString(R.string.advanced)
                else -> null
            }

            if (craftSkill != null) {
                Log.d("TAG", "upload craft skill: ")
                viewModel.updateCraftSkill(craftSkill)
                findNavController().navigate(R.id.action_craftSkillFragment_to_userIdFragment)
            } else {
                Snackbar.make(binding.root, R.string.select_craft_skill, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}