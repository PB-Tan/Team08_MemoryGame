package iss.nus.edu.sg.webviews.memorygame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import iss.nus.edu.sg.webviews.memorygame.databinding.FragmentSignInBinding
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.btnSignInFinal.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call backend in a background thread
            Thread {
                try {
                    val url = URL("http://10.0.2.2:5184/api/Users/signin")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true

                    // Prepare JSON body
                    val jsonBody = JSONObject()
                    jsonBody.put("username", username)
                    jsonBody.put("password", password)

                    OutputStreamWriter(connection.outputStream).use { writer ->
                        writer.write(jsonBody.toString())
                        writer.flush()
                    }

                    val responseCode = connection.responseCode
                    val responseText = connection.inputStream.bufferedReader().readText()

                    requireActivity().runOnUiThread {
                        if (responseCode == 200) {
                            val jsonResponse = JSONObject(responseText)
                            val userId = jsonResponse.optInt("userId", -1)
                            val message = jsonResponse.optString("message", "No message from server")

                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                            if (userId != -1) {
                                // Navigate to next fragment and pass username
                                val bundle = Bundle()
                                bundle.putString("USERNAME", username)
                                findNavController().navigate(
                                    R.id.action_signInFragment_to_fetchFragment,
                                    bundle
                                )
                            }
                        } else {
                            Toast.makeText(requireContext(), "Sign in failed: $responseCode", Toast.LENGTH_SHORT).show()
                        }
                    }

                    connection.disconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }

        binding.btnCreateAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Account creation handled automatically", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
