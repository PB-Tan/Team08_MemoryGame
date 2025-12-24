package iss.nus.edu.sg.webviews.memorygame

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import iss.nus.edu.sg.webviews.memorygame.databinding.FragmentPlayBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayBinding.inflate(inflater, container, false)
        username = arguments?.getString("USERNAME") ?: "GuestUser"

        setupGame()
        startTimer()

        return binding.root
    }

    private fun setupGame() {
        val baseColors = listOf(Color.RED, Color.BLUE, Color.GREEN)
        colors.clear()
        colors.addAll(baseColors)
        colors.addAll(baseColors)
        colors.shuffle()

        binding.gridLayout.removeAllViews()
        buttons.clear()

        for (i in 0 until 6) { // 6 buttons for demo; adjust for 6x6 grid
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

        if (firstSelected == null) firstSelected = button
        else if (secondSelected == null) {
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

        // Game completed
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
                    if (responseCode == 200) Toast.makeText(requireContext(), "Score recorded!", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(requireContext(), "Failed to record score", Toast.LENGTH_SHORT).show()
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
        _binding = null
    }
}
