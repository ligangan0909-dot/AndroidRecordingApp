package com.example.recordingapp.ui.playback

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.recordingapp.RecordingApp
import com.example.recordingapp.audio.AudioPlayer
import com.example.recordingapp.data.model.TranscriptionState
import com.example.recordingapp.databinding.FragmentPlaybackBinding
import kotlinx.coroutines.launch
import java.io.File

class PlaybackFragment : Fragment() {
    private var _binding: FragmentPlaybackBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var audioPlayer: AudioPlayer
    private vapackage com.example.recordingapp.ui.playback

import android.os.Bundle
import android.ose 
import android.os.Bundle
import a   
    privimport android.os.Handlbaimport android.os.Looper  import android.view.Laytiimport android.view.View
import ap
impor   ViewModelProviderimport android.widget.SeekBaorimport android.widget.Toast
[Pimport androidx.fragass.javaimport androidx.lifecycle.ViewModelPeeimport androidx.lifecycle.lifecycleScope
i oimport androidx.navigation.fragment.finlaimport com.example.recordingapp.RecordingApp
import   import com.example.recordingapp.audio.Audio  import com.example.recordingapp.data.model.Tranw(
import com.example.recordingapp.databinding.FragmentPlaybackupimport kotlinx.coroutines.launch
import java.io.File

class Playbainimport jmentPlaybackBinding.infla
class PlaybackFraine    private var _binding: Fragmenroot
    private val binding get() = _binding!!
    
    privns    
    private lateinit var audioPlayerew   at    private vapackage com.example.recordingapp.udi
import android.os.Bundle
import android.ose 
import   setupimport aControls()
      import android.os.nsimport a   
    privimpg(    privimobimport ap
impor   ViewModelProviderimport android.widget.SeekBaorimport android.widget.Toast
pToolbar() {
        bindi[Pimport androidx.fragass.javaimport androidx.lifecycle.ViewModelPeeimport androieUi oimport androidx.navigation.fragment.finlaimport com.example.recordingapp.RecordingApp
import   import lickimport   import com.example.recordingapp.audio.Audio  import com.example.recordingapp.dOnimport com.example.recordingapp.databinding.FragmentPlaybackupimport kotlinx.coroutines.launch
imporChanimport java.io.File

class Playbainimport jmentPlaybackBinding.infla
class PlaybackFraine      
class Playbainimpdioclass PlaybackFraine    private var _binding:       private val binding get() = _binding!!   }
               
    privns    
    private lateinit on   rt    private h(simport android.os.Bundle
import android.ose 
import   setupimport aControls()
      import a  import android.os     audimport   setupimpokC      e = {
            isPlaying    privimpg(    privimobimport ap
iseimpor   ViewModelProviderimport a.dpToolbar() {
        bindi[Pimport androidx.fragass.javaimport androidx.lifecycle
         bin  import   import lickimport   import com.example.recordingapp.audio.Audio  import com.example.recordingapp.dOnimport com.example.recordingapp.databinding.FragmentPlaybackupimport kotli  imporChanimport java.io.File

class Playbainimport jmentPlaybackBinding.infla
class PlaybackFraine      
class Playbainimpdioclass PlaybackFraine    private var _binding:       private val binding get()ui
class Playbainimport jment??"class PlaybackFraine      
class Playbainimpdietclass PlaybaikListener
                   
    privns    
    private lateinit on   rt    private h(simport android.os.Bundle
import android      privns    og    private l.simport android.ose 
import   setupimport aControls()
      import   imporvate fun loadRe      import a  import androidth             isPlaying    privimpg(    privimobimport ap
iseimpor   Vifileiseimpor   ViewModelProviderimport a.dpToolbar() {
   Ex        bindi[Pimport androidx.fragass.javaimporte(         bin  import   import lickimport   isePlayback()
        } el
class Playbainimport jmentPlaybackBinding.infla
class PlaybackFraine      
class Playbainimpdioclass PlaybackFraine    private var _binding:       private val binding get()ui
class Playbainimport jment??"class PlaybackFraine 
  class PlaybackFraine      
class Playbainimpdi??lass PlaybainimpdioclassH_class Playbainimport jment??"class PlaybackFraine      
class PlaybainimpdietclaaPlayer == null) {
 class Playbainimpdietclass PlaybaikListener
          se                   
yer.getDuration()
          privns    
   sp    pr        }import android      privns    og    private l.simport android.ose   import   setupimport aControls()
      import   imporvate fun loa(a      import   imporvate fun use)iseimpor   Vifileiseimpor   ViewModelProviderimport a.dpToolbar() {
   Ex        bindi[Pimport androidx.fragass.javaimport     Ex        bindi[Pimport androidx.fragass.javaimporte(         bce        } el
class Playbainimport jmentPlaybackBinding.infla
class PlaybackFraine      
class Playbainimpdioclae class Playbeeclass PlaybackFraine      
class Playbainimpdiayclass PurrentPosition()
   class Playbainimport jment??"class PlaybackFraine 
  class PlaybackFraine      
class Playbainimpd f  class PlaybackFraine      
class Playbainimpdaudiclass Playbainimpdi??lass )
class PlaybainimpdietclaaPlayer == null) {
 class Playbainimpdietclass PlaybaikListener
          se(cur class Playbainimpdietclass PlaybaikListerm          se                   
yer.getDurarmyer.getDuration()
          prin          privnsse   sp    pr        s       import   imporvate fun loa(a      import   imporvate fun use)iseimpor   Vifileiseimpor   ViewModelProviderimport d:   Ex        bindi[Pimport androidx.fragass.javaimport     Ex        bindi[Pimport androidx.fragass.javaimporte(         bce        }  class Playbainimport jmentPlaybackBinding.infla
class PlaybackFraine      
class Playbainimpdioclae class Playbeeclass PlaybackFraine  viclass PlaybackFraine      
class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass PurrentPosition()
   class Playbainiil   class Playbainimport jment??"class Playbsc  class PlaybackFraine      
class Playbainimpd f  cdiclass Playbainimpd f  class"?lass Playbainimpdaudiclass Playbainimpdi?ate funclass PlaybainimpdietclaaPlayer == null) {
 classcl class Playbainimpdietclass PlaybaikListe            se(cur class Playbainimpdietclasse yer.getDurarmyer.getDuration()
          prin          privnsse   sp    pr        s                  pri      binding.tranclass PlaybackFraine      
class Playbainimpdioclae class Playbeeclass PlaybackFraine  viclass PlaybackFraine      
class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass PurrentPosition()
   class Playbainiil   class Playbainimport jment??"class Playbsc  class PlaybackFraine      
class Playbainimpd f  cdiclass Playbainimpd f  classViclass Playbainimpdioclae   class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass PurrentPositin   class Playbainiil   class Playbainimport jment??"class Playbsc  class PlaybackFraine    ticlass Playbainimpd f  cdiclass Playbainimpd f  class"?lass Playbainimpdaudiclass Playbainim V classcl class Playbainimpdietclass PlaybaikListe            se(cur class Playbainimpdietclasse yer.getDurarmyer.getDuration()
          prin   an          prin          privnsse   sp    pr        s                  pri      binding.tranclass PlaybackFr= View.VISIBLE
     class Playbainimpdioclae class Playbeeclass PlaybackFraine  viclass PlaybackFraine      
class Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass PurrentPositns   class Playbainiil   class Playbainimport jment??"class Playbsc  class PlaybackFraine    .vclass Playbainimpd f  cdiclass Playbainimpd f  classViclass Playbainimpdioclae   class Playbfu          prin   an          prin          privnsse   sp    pr        s                  pri      binding.tranclass PlaybackFr= View.VISIBLE
     class Playbainimpdioclae class Playbeeclass PlaybackFraine  viclass PlaybackFraine      
class Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass PurrentPositns   class Playbainiil   class Playbainimport jment??"class Playbsc  class PlaybackFraine    .vclass Playbainimpd f  cdiclass Pl          class Playbainimpdioclae class Playbeeclass PlaybackFraine  viclass PlaybackFraine      
class Playbainimptoggclass   class Playbainim bclass Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbaini       class Playbainimpdioclae class Playbeeclass PlaybackFraine  viclass PlaybackFraine      
class Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass PurrentPositns   class Playbainiil   class Playbainimport jment??"class Playbsc  class PlaybackFraine    .vclass Playbainimpd f  cdiclass Pl          class Playbainimpdioclae class Playbeeclass PlaybackFraine  viclass PlaybackFraate.error.message ?class Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbainionclass Playbainimptoggclass   class Playbainim bclass Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbaini       class Playbainimpdioclae class Playbeeclass PlaybackFraine  viclass PlaybackFraine      
class Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass Pu  class Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass PurrentPositns   class Playbainiil   class Playbainimport jment??"class Playbsc  class PlaybackFraine    .vclass Playbainimpd f  ONclass Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass Pu  class Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass PurrentPositns   class Playbainiil   class Playbainimport jment??"class Playbsc  class PlaybackFraine    .vclass Playbainimpd f  ONclass Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass Pu  class Playbainimptoggclass   class Playbainimptoggclass Playbainimpdioclae ??class Playbainimpdiayclass PurrentPositns   class Playbainiil   class Playbainimport jment??"class Playbsc  class PlaybackFraine    .vclass Playbainimp{
                    binding.toggleViewButton.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun loadExistingTranscription() {
        val recordingId = arguments?.getLong("recordingId", 0L).toString()
        val filePath = arguments?.getString("filePath") ?: return
        val file = File(filePath)
        
        if (file.exists()) {
            viewModel.loadExistingTranscription(recordingId, file)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayback()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateSeekBarRunnable)
        audioPlayer.release()
        _binding = null
    }
}
