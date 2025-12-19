package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FetchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fetch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val playBtn = findViewById<Button>(R.id.play_button)
        playBtn.setOnClickListener {
            // TODO: implement play button later once 6 images have been selected
            // TODO: implement fetch images activity
            // The Fetch activity allows a URL to be specified. Clicking on the Fetch button will extract
            // the first 20 images that it finds on the webpage that the URL points to and display the
//                    downloaded images in a grid. A progress-bar should show the number of images
//            downloaded so far with descriptive text (e.g. Downloading 10 of 20 images …)
            //The user can change the URL in the middle of a download and click on the Fetch button
            //again. The current download would then be aborted and all images in the grid will be
            //cleared. The Fetch activity will download a new set of 20 images based on the new URL
            //entered

            //Once the first 20 images have been downloaded, allow the user to select 6 of them.

            val intent = Intent(this, PlayActivity::class.java)
            startActivity(intent)
        }
    }
}