package android.team08_memorygame

import android.R.attr.password
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.team08_memorygame.databinding.ActivityLoginBinding
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL
import kotlin.jvm.java

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        window.sharedElementEnterTransition =
            android.transition.TransitionInflater.from(this)
                .inflateTransition(android.R.transition.move)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContentView(binding.root)

        val cm = CookieManager(null, CookiePolicy.ACCEPT_ALL)
        CookieHandler.setDefault(cm)

        binding.loginButton.setOnClickListener {
            val mp = MediaPlayer.create(this@LoginActivity, R.raw.click_sound)
            mp.setOnCompletionListener { it.release() }
            mp.start()

            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Username or Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Thread {
                val (ok, message, userId, isPaid) = doLogin(username, password)
                UserManager.userIsPremium = isPaid
                UserManager.userId = userId.toString()
                runOnUiThread {
                    if (!ok) {
                        Toast.makeText(this, message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    val intent = Intent(this, FetchActivity::class.java)
                    startActivity(intent)
                }
            }.start()
        }
    }

    private fun doLogin(username: String, password: String): Quad<Boolean, String?, Int, Boolean> {
        var conn: HttpURLConnection? = null

        return try {
            val url = URL("http://10.0.2.2:5000/api/auth/login")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val bodyJson = JSONObject().apply {
                put("username", username)
                put("password", password)
            }.toString()

            conn.outputStream.use { os ->
                os.write(bodyJson.toByteArray(Charsets.UTF_8))
            }

            val stream =
                if (conn.responseCode in 200..299) conn.inputStream
                else conn.errorStream
            val responseText =
                BufferedReader(InputStreamReader(stream)).use { it.readText() }

            val res = JSONObject(responseText)

            // mapping over from backend
            val success = res.optBoolean("success", res.optBoolean("Success", false))
            val msg = res.optString("message", res.optString("Message", null))
            val userId = res.optInt("userId", res.optInt("UserId", 0))
            val isPaid = res.optBoolean("isPaid", res.optBoolean("IsPaid", false))

            Quad(success, msg, userId, isPaid)
        } catch (e: Exception) {
            Quad(false, e.message, 0, false)
        } finally {
            conn?.disconnect()
        }
    }
}

//Before the game can be played, a user must login with his username and password. A
//Login activity presents the login screen to the user when the app starts.
//2. Authenticate the provided user’s credentials with your backend .NET application. If
//authentication fails, inform the user accordingly.