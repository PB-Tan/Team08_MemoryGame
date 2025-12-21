package android.team08_memorygame

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.playactivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val scoreButton= findViewById<Button>(R.id.score_button)
        scoreButton.setOnClickListener {
            MediaPlayer.create(this, R.raw.button_sound).start()
            // TODO: implement memory game activity later
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        val backButton= findViewById<ImageButton>(R.id.back)
        backButton.setOnClickListener {
            MediaPlayer.create(this, R.raw.button_sound).start()
            val intent = Intent(this, FetchActivity::class.java)
            startActivity(intent)
        }

//        val gridView=findViewById<GridView>(R.id.playgrid)
//        val images=listOf(
//            R.drawable.question_mark,
//            R.drawable.question_mark,
//            R.drawable.question_mark,
//            R.drawable.question_mark,
//            R.drawable.question_mark,
//            R.drawable.question_mark)
    }
}

//The Play activity uses the 6 selected images for the game play. At the start, it displays 12
//placeholders (no images). Then when a placeholder is touched, it reveals the image
//behind that placeholder. It then waits for a second placeholder to be touched and reveals
//that too. If both images are identical, it leaves both images as they are (revealed to the
//user). If they are different, it hides the two images and reverts to showing the two
//placeholders.