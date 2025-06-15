package com.example.handicraft.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.handicraft.databinding.FragmentChatsBinding


class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
//    private val viewModel: ChatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val adapter = ChatAdapter { chat ->
//            val action = ChatsFragmentDirections.actionChatsFragmentToChatFragment(chat.id)
//            findNavController().navigate(action)
//        }
//
//        binding.recyclerView.apply {
//            layoutManager = LinearLayoutManager(context)
//            this.adapter = adapter
//        }

//        viewModel.chats.observe(viewLifecycleOwner) { chats ->
//            adapter.submitList(chats)
//        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}