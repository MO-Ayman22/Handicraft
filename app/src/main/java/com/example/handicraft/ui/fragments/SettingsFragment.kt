package com.example.handicraft_graduation_project_2025.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentSettingsBinding
import com.example.handicraft_graduation_project_2025.utils.SharedPrefUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.editAccount.setOnClickListener {
            ////findNavController().navigate(R.id.action_settingsFragment_to_editAccountFragment)
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save notification preference
        }

        binding.passwordManager.setOnClickListener {
            ////findNavController().navigate(R.id.action_settingsFragment_to_passwordManagerFragment)
        }

        binding.blockedAccounts.setOnClickListener {
            ////findNavController().navigate(R.id.action_settingsFragment_to_blockedAccountsFragment)
        }

        binding.logout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    SharedPrefUtil.clearUid(requireContext())
                   //// findNavController().navigate(R.id.action_settingsFragment_to_signInFragment)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}