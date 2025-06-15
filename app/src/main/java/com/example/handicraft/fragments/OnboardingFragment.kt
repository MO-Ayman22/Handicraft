package com.example.handicraft.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.handicraft.R
import com.example.handicraft.adapters.OnboardingAdapter
import com.example.handicraft.databinding.FragmentOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = OnboardingAdapter()
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.indicator, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    2 -> {
                        binding.nextButton.text = getString(R.string.get_started)
                        binding.skipButton.visibility = View.GONE
                        binding.signInText.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.nextButton.text = getString(R.string.next)
                        binding.skipButton.visibility = View.VISIBLE
                        binding.signInText.visibility = View.GONE
                    }
                }
            }
        })

        binding.nextButton.setOnClickListener {
            if (binding.viewPager.currentItem == 2) {
                findNavController().navigate(R.id.action_onboardingFragment_to_signUpFragment)
            } else {
                binding.viewPager.currentItem += 1
            }
        }

        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_signInFragment)
        }

        binding.signInText.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_signInFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


