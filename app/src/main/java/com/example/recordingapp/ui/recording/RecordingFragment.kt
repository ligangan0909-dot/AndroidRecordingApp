package com.example.recordingapp.ui.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.recordingapp.databinding.FragmentRecordingBinding

/**
 * RecordingFragment - Main recording interface
 * Will contain circular recording button and waveform visualization
 */
class RecordingFragment : Fragment() {
    private var _binding: FragmentRecordingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Setup recording functionality in task 3
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
