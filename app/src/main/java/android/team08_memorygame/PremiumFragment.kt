package android.team08_memorygame

import android.R.style
import android.os.Bundle
import android.team08_memorygame.databinding.FragmentPremiumBinding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController

class PremiumFragment : DialogFragment() {
    private var _binding: FragmentPremiumBinding?=null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // this makes this fragment full screen
        setStyle(STYLE_NORMAL,android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPremiumBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        //Payment Logic
        binding.buyPremiumButton.setOnClickListener {

        }


        //Close button press
        binding.closeButton.setOnClickListener {
            // navigate back to the ad banner, cleans everything in between -> webview and premium
            // this destroys the webviews that are in between making this memory efficient
            findNavController().popBackStack(R.id.adBannerFragment,false)
        }
    }
}