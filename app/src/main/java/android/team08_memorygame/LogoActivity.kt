package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat


class LogoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.sharedElementEnterTransition =
            android.transition.TransitionInflater.from(this)
                .inflateTransition(android.R.transition.move)

        setContentView(R.layout.activity_logo)

        val logo = findViewById<ImageView>(R.id.logopage)

        logo.postDelayed({
            val intent = Intent(this, LoginActivity::class.java)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                logo,
                "app_logo"
            )

            startActivity(intent, options.toBundle())
            finish()
        }, 2000) // splash delay
    }
}
