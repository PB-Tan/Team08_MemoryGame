package iss.nus.edu.sg.webviews.memorygame

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import iss.nus.edu.sg.webviews.memorygame.databinding.FragmentFetchBinding
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class FetchFragment : Fragment() {

    private var _binding: FragmentFetchBinding? = null
    private val binding get() = _binding!!
    private var username: String? = null

    private var adHandler: Handler = Handler(Looper.getMainLooper())
    private var adRunnable: Runnable? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString("USERNAME") ?: "GuestUser"
    }
    private fun startAdBanner() {
        adRunnable = object : Runnable {
            override fun run() {
                fetchAd() // fetch a new ad each time
                adHandler.postDelayed(this, 4000) // repeat every 4 seconds
            }
        }
        adHandler.post(adRunnable!!)
    }


    private fun fetchAd() {
        Thread {
            try {
                val url = URL("http://10.0.2.2:5184/api/Ads/random")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val response = conn.inputStream.bufferedReader().readText()
                val adText = JSONObject(response).getString("content") // matches backend

                requireActivity().runOnUiThread {
                    binding.tvAdBanner.text = adText
                }

                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }



    private fun showAdDialog(adText: String) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Advertisement")
            .setMessage(adText)
            .setPositiveButton("Close", null)
            .create()

        dialog.show()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFetchBinding.inflate(inflater, container, false)

        binding.tvFetch.text = "Welcome to Fetch Area, $username!"
        if (username == "GuestUser") {
            binding.tvAdBanner.visibility = View.VISIBLE
            startAdBanner()
        }

        binding.tvFetch.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("USERNAME", username)
            findNavController().navigate(R.id.action_fetchFragment_to_playFragment, bundle)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adRunnable?.let { adHandler.removeCallbacks(it) }
        _binding = null
    }
}
