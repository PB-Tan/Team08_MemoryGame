package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LeaderboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leaderboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fetchBtn = findViewById<Button>(R.id.fetch_button)
        fetchBtn.setOnClickListener {
            // TODO: displays leaderboard and return to fetch once done
            val intent = Intent(this, FetchActivity::class.java)
            startActivity(intent)
        }
    }
}

//When all images match, the game ends. The user’s completion time is sent to the .NET
//app and stored in its database. The Leaderboard activity is also automatically displayed to
//show the best 5 completion times, along with their usernames

//When the Leaderboard activity is closed, the user is returned to the Fetch Activity. The
//user can enter a different URL in the Fetch Activity and play the game again.