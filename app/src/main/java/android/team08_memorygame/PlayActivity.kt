package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val leaderboardBtn = findViewById<Button>(R.id.leader_button)
        leaderboardBtn.setOnClickListener {
            // TODO: implement memory game activity later
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }
    }
}

//The Play activity uses the 6 selected images for the game play. At the start, it displays 12
//placeholders (no images). Then when a placeholder is touched, it reveals the image
//behind that placeholder. It then waits for a second placeholder to be touched and reveals
//that too. If both images are identical, it leaves both images as they are (revealed to the
//user). If they are different, it hides the two images and reverts to showing the two
//placeholders.