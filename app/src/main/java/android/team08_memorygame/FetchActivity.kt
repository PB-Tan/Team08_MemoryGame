package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.app.AlertDialog
import android.net.Uri

class FetchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fetch)

        val btnDownload = findViewById<Button>(R.id.btnDownload)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val etUrl = findViewById<EditText>(R.id.etUrl)
        val imgView = findViewById<ImageView>(R.id.imgView)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fetchactivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fromReplay = intent.getBooleanExtra("FROM_REPLAY", false)
        if (fromReplay) {
            showWelcomePopup()
        }

        btnDownload.setOnClickListener {
            val url = etUrl.text.toString()
            if (url.isNotEmpty()) {
                downloadImage(url, imgView)
            }
        }

        btnDelete.setOnClickListener {
            deleteAllImages(imgView)
        }

//        val playBtn = findViewById<Button>(R.id.play_button)
//        playBtn.setOnClickListener {
//            // TODO: implement play button later once 6 images have been selected
//            // TODO: implement fetch images activity
//            // The Fetch activity allows a URL to be specified. Clicking on the Fetch button will extract
//            // the first 20 images that it finds on the webpage that the URL points to and display the
////                    downloaded images in a grid. A progress-bar should show the number of images
////            downloaded so far with descriptive text (e.g. Downloading 10 of 20 images …)
//            //The user can change the URL in the middle of a download and click on the Fetch button
//            //again. The current download would then be aborted and all images in the grid will be
//            //cleared. The Fetch activity will download a new set of 20 images based on the new URL
//            //entered
//
//            //Once the first 20 images have been downloaded, allow the user to select 6 of them.
//
//            val intent = Intent(this, PlayActivity::class.java)
//            startActivity(intent)
//        }
    }
    private fun downloadImage(urlStr: String, imgView: ImageView)
    {
        Thread {
            try {
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.connect()

                val input = conn.inputStream
                val file = File(filesDir, "img1.jpg")
                val output = FileOutputStream(file)

                input.copyTo(output)

                input.close()
                output.close()

                runOnUiThread {
                    imgView.setImageURI(Uri.fromFile(file))
                    Toast.makeText(this, "下载成功", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
    private fun deleteAllImages(imgView: ImageView) {
        val dir = filesDir
        dir.listFiles()?.forEach {
            if (it.name.endsWith(".jpg")) {
                it.delete()
            }
        }
        imgView.setImageDrawable(null)
        Toast.makeText(this, "图片已删除", Toast.LENGTH_SHORT).show()
    }


    private fun showWelcomePopup() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Welcome back!")
            .setMessage("Ready to start a new game?")
            .setCancelable(false)
            .setPositiveButton("Ok")
            { dialog, _ -> dialog.dismiss()}

            .setNegativeButton("Cancel")
            { _, _ ->finish() }.create()

        dialog.show()
    }
}