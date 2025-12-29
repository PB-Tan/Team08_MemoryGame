package android.team08_memorygame


import android.content.Intent
import android.os.Bundle
import android.team08_memorygame.databinding.ActivityLoginBinding
import android.widget.Toast
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

data class LoginResult (val success: Boolean, val message: String)
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
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Username or Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Thread {
                val result = doLogin(username, password)
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()

                    if (result.success) {
                        val intent = Intent(this, FetchActivity::class.java)
                        startActivity(intent)
                    }
                }
            }.start()
        }
    }

    private fun doLogin(username: String, password: String): LoginResult  {
        try {
            //HTTP request by passing the credentials in query strings
            val url = URL("http://10.0.2.2:5000/api/Auth2/login")

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST" // We are calling a [HttpPost("login")] endpoint
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true // We will write a request body (POST body) to the server
                // Tell the server what format we are sending in the request body
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            // Build the POST body as key=value&key=value (form-urlencoded style)
            val body = "reqUsername=$username&reqPassword=$password"
            //    .use { } automatically closes the stream after writing (like a safe resource cleanup)
            conn.outputStream.use { os ->
                os.write(body.toByteArray(Charsets.UTF_8))
            }

            // Choose the correct stream to read:
            //  - inputStream for success responses (2xx)
            //  - errorStream for failure responses (4xx/5xx)
            //  Without this, reading inputStream on failure can throw and you lose the error message
            val stream =
                if (conn.responseCode in 200..299) conn.inputStream
                else conn.errorStream

            // reading the response body from the server
            val responseText =
                BufferedReader(InputStreamReader(stream)).use { it.readText() }
            // Unwrapping the response body using JSON body
            JSONObject(responseText).apply {
                val success = optBoolean("success", false)
                val message = optString("message", "unknown response")
                val isPaidUser = optBoolean("ispaiduser", false)
                val username = optString("username", "unknown user")

                //set usermanager to the values retrieved from HTTP response body
                UserManager.setPremiumStatus(isPaidUser)
                UserManager.username = username
                return LoginResult(success, message)
            }
        }catch(e: Exception) {
            e.printStackTrace()
            return LoginResult(false, "Network error: ${e.message ?: "unknown"}")
        }
    }
}

//Before the game can be played, a user must login with his username and password. A
//Login activity presents the login screen to the user when the app starts.
//2. Authenticate the provided user’s credentials with your backend .NET application. If
//authentication fails, inform the user accordingly.