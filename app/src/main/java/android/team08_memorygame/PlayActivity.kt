package android.team08_memorygame
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.HttpURLConnection
import java.net.URL

class PlayActivity : AppCompatActivity() {
    private var cardList = mutableListOf<Card>()
    private lateinit var adapter: MemoryAdapter
    private var seconds = 0 // current time
    private var running = false
    private var hasStarted = false


    private var firstSelectedPosition: Int = -1 //dont choose a pic
    private var isBusy = false
    private lateinit var timeTextView: TextView

    private lateinit var stopButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        timeTextView = findViewById(R.id.timeTextView)

        stopButton = findViewById(R.id.stop_button)

        // check leaderboard
        val leaderboardBtn = findViewById<Button>(R.id.leader_button)
        leaderboardBtn.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }
        setupGame() }
    private fun setupGame() {
        val images = listOf(
            R.drawable.img_1,
            R.drawable.img_2,
            R.drawable.img_3,
            R.drawable.img_4,
            R.drawable.img_5,
            R.drawable.img_6
        )
        val allImages = (images + images).shuffled()
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
//game
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
    if (isBusy || currentCard.isFaceUp || currentCard.isMatched) return
    currentCard.isFaceUp = true

    adapter.notifyDataSetChanged()

        if (firstSelectedPosition == -1) {
            firstSelectedPosition = position
        } else {
            val firstCard = cardList[firstSelectedPosition]
            if (firstCard.imageId == currentCard.imageId) {
                firstCard.isMatched = true
                currentCard.isMatched = true
                firstSelectedPosition = -1
                checkWin()
            } else {
                isBusy = true
                Handler(Looper.getMainLooper()).postDelayed({
                    firstCard.isFaceUp = false
                    currentCard.isFaceUp = false
                    firstSelectedPosition = -1
                    isBusy = false
                    adapter.notifyDataSetChanged()
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


