package android.team08_memorygame

import android.os.Bundle
import android.team08_memorygame.databinding.FragmentFetchBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class FetchFragment : Fragment() {

    private var _binding: FragmentFetchBinding? = null
    private val binding get() = _binding!!
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString("USERNAME") ?: "GuestUser"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFetchBinding.inflate(inflater, container, false)

        // Show welcome text
        binding.tvFetch.text = "Welcome to Fetch Area, $username!"

        // Navigate to PlayActivity (via PlayFragment in nav_graph)
        binding.tvFetch.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("USERNAME", username)
            findNavController().navigate(R.id.action_fetchFragment_to_playFragment, bundle)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
