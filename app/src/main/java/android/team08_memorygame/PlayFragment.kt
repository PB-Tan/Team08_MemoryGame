package android.team08_memorygame

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.team08_memorygame.databinding.FragmentPlayBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PlayFragment : Fragment() {

    private var _binding: FragmentPlayBinding? = null
    private val binding get() = _binding!!

    private val buttons = mutableListOf<Button>()
    private val colors = mutableListOf<Int>()
    private var firstSelected: Button? = null
    private var secondSelected: Button? = null

    private var username: String = "GuestUser"
    private var secondsElapsed = 0
    private var timer: CountDownTimer? = null

    private val adHandler = Handler(Looper.getMainLooper())
    private var adRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayBinding.inflate(inflater, container, false)
        username = arguments?.getString("USERNAME") ?: "GuestUser"

        setupGame()
        startTimer()

        // Show ad only for GuestUser
        if (username == "GuestUser") {
            startAdBanner()
        } else {
            binding.tvAdBanner.visibility = View.GONE
        }

        return binding.root
    }

    private fun startAdBanner() {
        adRunnable = object : Runnable {
            override fun run() {
                fetchAd()
                adHandler.postDelayed(this, 4000)
            }
        }
        adHandler.post(adRunnable!!)
    }

    private fun fetchAd() {
        Thread {
            try {
                val url = URL("http://10.0.2.2:5184/api/Ads/api/Ads/random")

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val response = conn.inputStream.bufferedReader().readText()
                val adText = JSONObject(response).getString("content")

                requireActivity().runOnUiThread {
                    binding.tvAdBanner.visibility = View.VISIBLE
                    binding.tvAdBanner.text = adText
                    binding.tvAdBanner.setOnClickListener {
                        showAdDialog(adText)
                    }
                }

                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun showAdDialog(adText: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Advertisement")
            .setMessage(adText)
            .setPositiveButton("Close", null)
            .create()
        dialog.show()
    }

    private fun setupGame() {
        val baseColors = listOf(Color.RED, Color.BLUE, Color.GREEN)
        colors.clear()
        colors.addAll(baseColors)
        colors.addAll(baseColors)
        colors.shuffle()

        binding.gridLayout.removeAllViews()
        buttons.clear()

        for (i in 0 until 6) {
            val btn = Button(requireContext())
            btn.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            btn.setBackgroundColor(Color.GRAY)
            btn.setOnClickListener { onButtonClicked(btn, i) }
            buttons.add(btn)
            binding.gridLayout.addView(btn)
        }
    }

    private fun onButtonClicked(button: Button, index: Int) {
        if (button == firstSelected || button == secondSelected) return
        button.setBackgroundColor(colors[index])

        if (firstSelected == null) {
            firstSelected = button
        } else if (secondSelected == null) {
            secondSelected = button
            button.postDelayed({ checkMatch() }, 500)
        }
    }

    private fun checkMatch() {
        if (firstSelected != null && secondSelected != null) {
            val index1 = buttons.indexOf(firstSelected!!)
            val index2 = buttons.indexOf(secondSelected!!)
            if (colors[index1] == colors[index2]) {
                firstSelected!!.isEnabled = false
                secondSelected!!.isEnabled = false
            } else {
                firstSelected!!.setBackgroundColor(Color.GRAY)
                secondSelected!!.setBackgroundColor(Color.GRAY)
            }
        }
        firstSelected = null
        secondSelected = null

        if (buttons.all { !it.isEnabled }) {
            stopTimer()
            Toast.makeText(requireContext(), "Congratulations $username! Game Completed!", Toast.LENGTH_LONG).show()
            recordScore(secondsElapsed)
        }
    }

    private fun startTimer() {
        secondsElapsed = 0
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsElapsed++
                binding.tvTimer.text = "Time: ${secondsElapsed}s"
            }

            override fun onFinish() {
                binding.tvTimer.text = "Time's up!"
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    private fun recordScore(totalSeconds: Int) {
        Thread {
            try {
                val url = URL("http://10.0.2.2:5184/api/GameResults")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonBody = JSONObject()
                jsonBody.put("username", username)
                jsonBody.put("timeTakenSeconds", totalSeconds)

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonBody.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                connection.disconnect()

                requireActivity().runOnUiThread {
                    if (responseCode == 200) {
                        Toast.makeText(requireContext(), "Score recorded!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to record score", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
        adRunnable?.let { adHandler.removeCallbacks(it) }
        _binding = null
    }
}
