package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.jvm.java

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val loginBtn = findViewById<Button>(R.id.login_button)
        loginBtn.setOnClickListener {
            // TODO: validate login later
            val intent = Intent(this, FetchActivity::class.java)
            startActivity(intent)
        }
    }
}

//Before the game can be played, a user must login with his username and password. A
//Login activity presents the login screen to the user when the app starts.
//2. Authenticate the provided user’s credentials with your backend .NET application. If
//authentication fails, inform the user accordingly.