package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide


// PLAY TO SPLASH TO FETCH
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashmain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val images = intent.getStringArrayListExtra("images") ?: arrayListOf()

        // Loading the GIF using Glide
        val gifImage: ImageView = findViewById(R.id.splashGif)
        Glide.with(this)
            .asGif()
            .load(R.raw.giphy)
            .into(gifImage)

        // Timer: 2 seconds
        object : CountDownTimer(2400, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                // Start PlayActivity
                val intent = Intent(this@SplashActivity, PlayActivity::class.java)
                intent.putStringArrayListExtra("images", images)
                startActivity(intent)
                finish()
            }
        }.start()
    }
}