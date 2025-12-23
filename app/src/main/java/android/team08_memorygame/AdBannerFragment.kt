package android.team08_memorygame

import android.os.Bundle
import android.team08_memorygame.databinding.FragmentAdBannerBinding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController

class AdBannerFragment : Fragment() {
    private var _binding: FragmentAdBannerBinding?=null
    private val binding get()=_binding!!

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
        binding.apply{
            adImageView.setImageResource(R.drawable.ad)
            adImageView.setOnClickListener {
                root.findNavController().navigate(R.id.action_adBannerFragment_to_adWebViewFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}