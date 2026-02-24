package com.example.recordingapp.ui.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.recordingapp.databinding.FragmentFilesBinding

/**
 * FilesFragment - Displays list of recordings
 * Will contain search, filter, and card-based list
 */
class FilesFragment : Fragment() {
    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Setup files list functionality in task 6
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
