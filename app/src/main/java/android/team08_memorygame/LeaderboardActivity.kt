package android.team08_memorygame

import LeaderboardAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.os.StrictMode
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.jvm.java

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var leaderboardRecyclerView: RecyclerView

    private lateinit var podium1: View
    private lateinit var podium2: View
    private lateinit var podium3: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leaderboard)


        val backButton = findViewById<ImageButton>(R.id.back)
        backButton.setOnClickListener {
            val mp = MediaPlayer.create(this@LeaderboardActivity, R.raw.click_sound)
            mp.setOnCompletionListener { it.release() }
            mp.start()
            finish()
        }

        val replayButton = findViewById<AppCompatButton>(R.id.replay_button)
        replayButton.setOnClickListener {
            val mp = MediaPlayer.create(this@LeaderboardActivity, R.raw.click_sound)
            mp.setOnCompletionListener { it.release() }
            mp.start()

            val intent = Intent(this, FetchActivity::class.java)
            intent.putExtra("FROM_REPLAY", true)  // for the dialog box appeerance
            startActivity(intent)
            finish()
        }

        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView)
        leaderboardRecyclerView.layoutManager = LinearLayoutManager(this)

        podium1 = findViewById(R.id.podium1)
        podium2 = findViewById(R.id.podium2)
        podium3 = findViewById(R.id.podium3)

//        main thread snetwork -> just for debug purpose
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        fetchLeaderboard()
        animatePodiums()

        // Replay and Back button logic remains the same
    }



    private fun fetchLeaderboard() {
        try {
//            to reaches the PC we can use the 10.0.2: server
            val url = URL("http://10.0.2.2:5000/api/Scores/top5")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonArray = JSONArray(response.toString())

                // First we will create the leaderboard Items
                val leaderboardItems = mutableListOf<Score>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val name = item.getString("username")
                    val scoreTime = item.getInt("completionTimeSeconds")
                    leaderboardItems.add(Score(name, scoreTime))
                }

                //  podium heights for the top 3 players

                if (leaderboardItems.size >= 3) {
                    podium1.layoutParams.height = 650  // 1st place winner
                    podium2.layoutParams.height = 450  // 2nd place runner1
                    podium3.layoutParams.height = 300   // 3rd place runner 2
                }

                podium1.requestLayout()
                podium2.requestLayout()
                podium3.requestLayout()


                leaderboardRecyclerView.adapter = LeaderboardAdapter(leaderboardItems)

            }

            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


        private fun animatePodiums() {
        podium1.post {
            podium1.pivotY = podium1.height.toFloat()
            podium2.pivotY = podium2.height.toFloat()
            podium3.pivotY = podium3.height.toFloat()

            val a1 = ObjectAnimator.ofFloat(podium1, View.SCALE_Y, 0f, 1f).setDuration(800)
            val a2 = ObjectAnimator.ofFloat(podium2, View.SCALE_Y, 0f, 1f).setDuration(800)
            val a3 = ObjectAnimator.ofFloat(podium3, View.SCALE_Y, 0f, 1f).setDuration(800)

            AnimatorSet().apply {
                playSequentially(a2, a1, a3)
                start()
            }
        }
    }
}

//When all images match, the game ends. The user’s completion time is sent to the .NET
//app and stored in its database. The Leaderboard activity is also automatically displayed to
//show the best 5 completion times, along with their usernames

//When the Leaderboard activity is closed, the user is returned to the Fetch Activity. The
//user can enter a different URL in the Fetch Activity and play the game again.