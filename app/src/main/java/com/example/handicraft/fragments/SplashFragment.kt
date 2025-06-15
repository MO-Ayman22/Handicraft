package com.example.handicraft_graduation_project_2025.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.handicraft.R
import com.example.handicraft.databinding.FragmentSplashBinding
import com.example.handicraft_graduation_project_2025.ui.viewmodels.SplashViewModel


class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logo.startAnimation(
            android.view.animation.AnimationUtils.loadAnimation(
                context,
                R.anim.fade_in
            )
        )
        binding.appName.startAnimation(
            android.view.animation.AnimationUtils.loadAnimation(
                context,
                R.anim.fade_in
            )
        )

//        viewLifecycleOwner.lifecycleScope.launch {
//            delay(2000)
//            viewModel.checkUserLogin(requireContext())
//            viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
//                if (isLoggedIn) {
//                    findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
//                } else {
//                    findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
//                }
//            }
//    }

        findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}