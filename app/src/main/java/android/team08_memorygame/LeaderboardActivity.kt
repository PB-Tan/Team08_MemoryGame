package android.team08_memorygame

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.nio.file.Files.find

class LeaderboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leaderboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.leader)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val replayBtn = findViewById<Button>(R.id.replay_button)
        replayBtn.setOnClickListener {
            // TODO: displays leaderboard and return to fetch once done
            val intent = Intent(this, FetchActivity::class.java)
            startActivity(intent)
        }

//        animation for twoer podiums
        val podium1 = findViewById<View>(R.id.podium1)
        val podium2 = findViewById<View>(R.id.podium2)
        val podium3 = findViewById<View>(R.id.podium3)

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