package android.team08_memorygame

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.jvm.java

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.sharedElementEnterTransition =
            android.transition.TransitionInflater.from(this)
                .inflateTransition(android.R.transition.move)

        setContentView(R.layout.activity_login)

        val loginButton= findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            MediaPlayer.create(this, R.raw.button_sound).start()
            val intent = Intent(this, FetchActivity::class.java)
            startActivity(intent)
        }
    }
}

//Before the game can be played, a user must login with his username and password. A
//Login activity presents the login screen to the user when the app starts.
//2. Authenticate the provided user’s credentials with your backend .NET application. If
//authentication fails, inform the user accordingly.