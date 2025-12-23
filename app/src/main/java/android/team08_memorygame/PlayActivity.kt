package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.team08_memorygame.databinding.ActivityPlayBinding
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayBinding

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

        // -----ADAPTER SET UP------
        // get all files from app specific storage
        val allFiles = filesDir.listFiles()
        // get images that has these extensions
        val imageFiles = allFiles?.filter { it.extension == "jpg" || it.extension == "png"}?: emptyList()
        // take the first 6
        val chosenImages = imageFiles.take(6)
        // create pairs and shuffle
        // use it.name to pass names to adapter
        val boardImages = (chosenImages+chosenImages).map{ it.name }.shuffled()
        val adapter = MemoryBoardAdapter(this,boardImages)
        binding.rvBoard.adapter=adapter

        //-----CHECK PREMIUM STATUS-----
        if(UserManager.userIsPremium){
            binding.fragmentContainerView.visibility = View.GONE
        } else {
            binding.fragmentContainerView.visibility = View.VISIBLE
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