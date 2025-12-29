package android.team08_memorygame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.bumptech.glide.Glide


class LogoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.sharedElementEnterTransition =
            android.transition.TransitionInflater.from(this)
                .inflateTransition(android.R.transition.move)

        setContentView(R.layout.activity_logo)

        val logo = findViewById<ImageView>(R.id.logopage)
//        val gifUri = Uri.parse("android.resource://${packageName}/${R.raw.android_logo}")
//        Glide.with(this)
//            .asGif()
//            .load(gifUri)
//            .into(logo)

        logo.postDelayed({
            val intent = Intent(this, LoginActivity::class.java)


            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                logo,
                "app_logo"
            )

            startActivity(intent, options.toBundle())
            finish()
        }, 2600) // splash delay
    }
}
