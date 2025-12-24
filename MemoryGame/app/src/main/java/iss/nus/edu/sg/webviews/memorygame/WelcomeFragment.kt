package iss.nus.edu.sg.webviews.memorygame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import iss.nus.edu.sg.webviews.memorygame.databinding.FragmentWelcomeBinding
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        binding.btnSignin.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_signInFragment)
        }

        binding.btnGuest.setOnClickListener {
            val guestUsername = "GuestUser"
            val guestPassword = "guest"

            Thread {
                try {
                    val url = URL("http://10.0.2.2:5184/api/Users/signin")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true

                    val jsonBody = JSONObject()
                    jsonBody.put("username", guestUsername)
                    jsonBody.put("password", guestPassword)

                    OutputStreamWriter(connection.outputStream).use { writer ->
                        writer.write(jsonBody.toString())
                        writer.flush()
                    }

                    val responseCode = connection.responseCode
                    val responseText = connection.inputStream.bufferedReader().readText()

                    requireActivity().runOnUiThread {
                        if (responseCode == 200) {
                            val bundle = Bundle()
                            bundle.putString("USERNAME", guestUsername)
                            findNavController().navigate(R.id.action_welcomeFragment_to_fetchFragment, bundle)

                        } else {
                            Toast.makeText(requireContext(), "Guest login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    connection.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Guest login failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
