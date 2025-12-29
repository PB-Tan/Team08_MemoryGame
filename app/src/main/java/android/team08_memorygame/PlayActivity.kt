package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.team08_memorygame.databinding.ActivityPlayBinding
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.URL

class PlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayBinding

    private var cardList = mutableListOf<Card>()
    private lateinit var adapter: MemoryAdapter
    private var seconds = 0 // current time
    private var running = false
    private var hasStarted = false

    // game
    private var firstSelectedPosition: Int = -1
    private var isBusy = false
    private lateinit var timeTextView: TextView
    private lateinit var stopButton: Button


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

        //score_button
        val leaderboardBtn = findViewById<Button>(R.id.leader_button)
        leaderboardBtn.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        setupGame()
    }

    private fun setupGame() {
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

        val gridView = findViewById<GridView>(R.id.gridView)
        adapter = MemoryAdapter(this, cardList)
        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            onCardClicked(position)
        }
    }

    private fun onCardClicked(position: Int) {
    if (!hasStarted) { //first time play
        hasStarted = true
        running = true
        seconds = 0
        startTimerInBackground()
    } else if (!running) { //continue
        running = true
        startTimerInBackground()
    }



        val currentCard = cardList[position]
        // no return type so return means over
        if (isBusy || currentCard.isFaceUp || currentCard.isMatched) return
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
                }, 1000)
            }
        }

    //click buttons


        stopButton.setOnClickListener {
            running = false
        }

    }

    private fun startTimerInBackground() {
        Thread {
            while (running) {
                Thread.sleep(1000)
                seconds++
                runOnUiThread {
                    timeTextView.text = "Time: $seconds s"
                }
            }
        }.start()
    }

    private fun checkWin() {
        if (cardList.all { it.isMatched }) {
            Toast.makeText(this, "Congratulations!", Toast.LENGTH_SHORT).show()
            //when game is completed stop timing
            running = false

            sendTimeToDotNet(seconds)
        }
    }

    private fun sendTimeToDotNet(timeInSeconds: Int) {
        sendScore("player1", timeInSeconds)
    }

    private fun sendScore(username: String, timeInSeconds: Int) {
        val urlString =
            "http://10.0.2.2:5000/api/leaderboard?username=$username&time=$timeInSeconds"

        Thread {
            try {
                URL(urlString).openStream().close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

}