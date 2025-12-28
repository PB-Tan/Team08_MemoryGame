package android.team08_memorygame

import android.os.Bundle
import android.team08_memorygame.databinding.FragmentAdBannerBinding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

data class  AdContent(val imageResId: Int, val url: String)
class AdBannerFragment : Fragment() {
    private var _binding: FragmentAdBannerBinding?=null
    private val binding get()=_binding!!

    // hardcode advertisements for now
    private val adList = listOf(
        AdContent(R.drawable.ad, "https://www.google.com"),
        AdContent(R.drawable.ad_mc, "https://www.mcdonalds.com"),
        AdContent(R.drawable.ad_nike, "https://www.nike.com"),
    )

    // track which ad is showing
    private var currentAdIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdBannerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // start 30 sec timer loop here using the carousel feature Coroutines
        // safer than timers since they stop automatically when user closes the app
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                updateAdUI()
                // change this to 30 for real application. 5 for testing
                delay(5_000)
                // move to next index and loop back to 0 if at end
                currentAdIndex = (currentAdIndex + 1) % adList.size
            }
        }
    }

    private fun updateAdUI(){
        if(_binding == null) return
        val currentAd = adList[currentAdIndex]
        //update image
        binding.apply{
            adImageView.setImageResource(currentAd.imageResId)
            adImageView.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("url_to_load", currentAd.url)
                }
                findNavController().navigate(R.id.action_adBannerFragment_to_adWebViewFragment,bundle)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}