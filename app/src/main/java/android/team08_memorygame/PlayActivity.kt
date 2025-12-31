package android.team08_memorygame

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.team08_memorygame.databinding.ActivityPlayBinding
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


data class submitScoreResult (val success: Boolean, val message: String)
class PlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayBinding

    private var cardList = mutableListOf<Card>()
    private lateinit var adapter: MemoryAdapter
    private var seconds = 0 // Keep track of time for completionTimeSeconds
    private var pause: Boolean = false
    private var timerRunning = false
    private var timerThread: Thread? = null
    private var firstAttempt = true
    private var playProgress: Int = 0

    // game
    private var firstSelectedPosition: Int = -1
    private var isBusy = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // If user is Premium, HIDE ads (GONE).
        // If user is NOT Premium, SHOW ads (VISIBLE).
        if(UserManager.userIsPremium){
            binding.fragmentContainerView.visibility = View.GONE
        } else {
            binding.fragmentContainerView.visibility = View.VISIBLE
        }

        binding.apply {
            //pause button, either pause or run again
            stopButton.setOnClickListener {
                pause = !pause
                swapColor()
            }

        }
        setupGame()
    }

    private fun swapColor() {
        binding.apply {
            if (pause) {
                stopButton.text = "Continue"
                stopButton.setBackgroundColor(Color.parseColor("#93BD57"))
                timeTextView.setBackgroundColor(Color.parseColor("#F96E5B"))
            } else {
                stopButton.text = "Pause"
                stopButton.setBackgroundColor(Color.parseColor("#F96E5B"))
                timeTextView.setBackgroundColor(Color.parseColor("#93BD57"))
            }
        }
    }

    private fun setupGame() {
        if (firstAttempt) {
            pause = false
            startTimerInBackground()
        }
        val intentImages = intent.getStringArrayListExtra("images")

        if (intentImages == null || intentImages.size != 6) {
            Toast.makeText(this, "Game requires 6 images from selection", Toast.LENGTH_LONG).show()
            return
        }

        val images = intentImages.toList()

        //copy pictures
        val allImages = (images + images).shuffled()
        //put pics in a container
        cardList.clear()
        for (img in allImages) {
            cardList.add(Card(img))
        }
        
        adapter = MemoryAdapter(this, cardList)
        binding.apply {
            gridView.adapter = adapter
            gridView.setOnItemClickListener { _, _, position, _ ->
                onCardClicked(position)
            }
        }
    }

    private fun onCardClicked(position: Int) {
        val currentCard = cardList[position]
        // no return type so return means over
        if (isBusy || currentCard.isFaceUp || currentCard.isMatched || pause) return
        currentCard.isFaceUp = true
        adapter.notifyDataSetChanged() // refresh UI

        //choose pic
        if (firstSelectedPosition == -1) {
            // choose first one
            firstSelectedPosition = position
        } else {//already choose one pic
            val firstCard = cardList[firstSelectedPosition]
            if (firstCard.imageUrl == currentCard.imageUrl) {
                firstCard.isMatched = true
                currentCard.isMatched = true
                playProgress += 2
                binding.displayPlayProgress.text = " $playProgress /12"
                firstSelectedPosition = -1//clear
                checkWin()
            } else {
                // don't match
                isBusy = true // avoid click other places

                // postDelayed
                Handler(Looper.getMainLooper()).postDelayed({
                    firstCard.isFaceUp = false
                    currentCard.isFaceUp = false
                    firstSelectedPosition = -1
                    isBusy = false
                    adapter.notifyDataSetChanged() // refresh close pic
                }, 750)
            }
        }
    }

    private fun startTimerInBackground() {
        // prevent starting multiple timer threads
        if (timerRunning) return

        timerRunning = true
        timerThread = Thread {
            while (timerRunning) {
                if (!pause) {
                    try {
                        Thread.sleep(1000)
                    }catch (e: InterruptedException) {
                        break;
                    }
                    //time tracking
                    seconds++
                    runOnUiThread {
                        //display time passed in UI
                        binding.timeTextView.text = "Time: $seconds s"
                    }
                } else {
                    try {
                        Thread.sleep(200)
                    } catch (e: InterruptedException) {
                        break;
                    }
                }
            }
        }
        timerThread?.start()
    }

    // when player has won the game
    private fun checkWin() {
        if (cardList.all { it.isMatched }) {
            //when game is completed stop timing
            stopTimer()
            //post request to backend with the seconds
            sendTimeToDotNet(seconds)
            //navigate to leaderboard
            val intent = Intent(this@PlayActivity, LeaderboardActivity::class.java)


            startActivity(intent)
        }
    }

    private fun stopTimer() {
        timerThread?.interrupt()
        timerThread = null
        timerRunning = false
    }

    private fun sendTimeToDotNet(timeInSeconds: Int) {
        //retrieve session username and send it to backend
        val playerName = UserManager.username!!

        Thread {
            val result = sendScore(playerName, timeInSeconds)
            runOnUiThread {
                Toast.makeText(this@PlayActivity, result.message, Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun sendScore(username: String, timeInSeconds: Int): submitScoreResult {
        val urlString = URL("http://10.0.2.2:5000/api/Scores2")

        //Connect to UrlString and prepare header
        val conn = (urlString.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST" // We are calling a [HttpPost("login")] endpoint
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true // We will write a request body (POST body) to the server
            // Tell the server what format we are sending in the request body
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }

        //pass the username and completed time to the request body
        val requestBody = "reqUsername=$username&reqCompletionTimeSeconds=$timeInSeconds"

        conn.outputStream.use {
            os -> os.write(requestBody.toByteArray(Charsets.UTF_8))
        }

        val stream =
            if (conn.responseCode in 200..299) conn.inputStream
            else conn.errorStream

        val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }

        JSONObject(responseText).apply {
            val success = optBoolean("success", false)
            val message = optString("message", "unknown response")
            return submitScoreResult(success, message)
        }
    }
}