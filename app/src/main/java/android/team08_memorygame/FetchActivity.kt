package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.team08_memorygame.databinding.ActivityFetchBinding
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jsoup.Jsoup
import kotlin.random.Random

class FetchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFetchBinding

    // view for progress bar
    private lateinit var progressContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView

    // for simulated progress
    private val handler = Handler(Looper.getMainLooper())
    private val random = Random(System.currentTimeMillis())
    private var simulateRunnable: Runnable? = null
    private var totalImagesToDownload = 20
    private var downloadedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFetchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            recyclerView.layoutManager = GridLayoutManager(this@FetchActivity, 3)
            recyclerView.adapter = ImageAdapter()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressContainer = findViewById(R.id.progressContainer)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)

        // grid layout with 4 columns. adapter handles image loading + selection
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        adapter = ImageAdapter()
        recyclerView.adapter = adapter

        //If this is the second time user is playing
        val fromReplay = intent.getBooleanExtra("FROM_REPLAY", false)
        if (fromReplay) {
            showWelcomePopup()
        }

        // init buttons
        binding.apply {
            fetchButton.setOnClickListener {
                val pageUrl = urlField.text.toString()
                if (pageUrl.isNotEmpty()) {
                    fetchImages(pageUrl)
                }
            }

            startButton.setOnClickListener {
                val adapter = recyclerView.adapter as? ImageAdapter
                val selected = adapter?.getSelectedImages() ?: emptyList()
                if (selected.size != 6) {
                    Toast.makeText(this@FetchActivity, "please select 6 images", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // Pass the 6 images to PlayActivity
                val intent = Intent(this@FetchActivity, PlayActivity::class.java)
                intent.putStringArrayListExtra("images", ArrayList(selected))
                startActivity(intent)
            }

            deleteButton.setOnClickListener {
                deleteAllImages()
            }
        }
    }

    private fun fetchImages(pageUrl: String) {
        Thread {
            try {
                val urls = fetchImageUrls(pageUrl)
                    .distinct()
                    .filter { it.endsWith(".jpg") || it.endsWith(".png") }
                    .take(20)

                runOnUiThread {
                    (binding.recyclerView.adapter as? ImageAdapter)?.setImages(urls)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Parsing failed", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun fetchImageUrls(pageUrl: String): List<String> {
        val imageUrls = mutableListOf<String>()

        try {
            // 1. Connect to webpage (simulate browser)
            val doc = Jsoup.connect(pageUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(10_000)
                .get()

            // 2. Grab all <img> tags
            val imgs = doc.select("img")

            for (img in imgs) {

                // 3. Prioritize common image source attributes
                var src = img.absUrl("src")

                if (src.isEmpty()) {
                    src = img.absUrl("data-src")
                }

                if (src.isEmpty()) {
                    src = img.absUrl("data-lazy")
                }

                // 4. StockSnap special handling:
                //    Replace thumbnail URL with original URL (avoid CDN 403)
                if (src.contains("cdn.stocksnap.io")) {
                    src = src
                        .replace("/img-thumbs/280h/", "/img-originals/")
                        .replace("/img-thumbs/320h/", "/img-originals/")
                }

                // 5. Basic filtering
                if (
                    src.isNotEmpty() &&
                    (src.endsWith(".jpg") || src.endsWith(".jpeg") || src.endsWith(".png"))
                ) {
                    // 🔹 Extra filtering for StockSnap: keep only real photo thumbnails --- prev it kept downloading a smilely face
                    if (pageUrl.contains("stocksnap.io")) {
                        // their thumbnails live under /img-thumbs/
                        if (!src.contains("/img-thumbs/")) {
                            continue   // skip smileys, logos, other UI icons
                        }
                    }
                    imageUrls.add(src)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Re-throw or handle as needed, but the caller also catches Exception
            throw e
        }

        return imageUrls
    }
    private fun startSimulatedProgress(total: Int, delayMs: Long) {
        // cancel any old simulation
        cancelSimulatedProgress()

        totalImagesToDownload = total
        downloadedCount = 0

        progressBar.max = totalImagesToDownload
        progressBar.progress = 0
        tvProgress.text = "Downloading 0 of $totalImagesToDownload..."
        progressContainer.visibility = View.VISIBLE

        simulateNextStep(delayMs)
    }

    private fun deleteAllImages() {
        val dir = filesDir
        dir.listFiles()?.forEach {
            if (it.name.endsWith(".jpg")) {
                it.delete()
            }
        }

        // Clear the images in the adapter
        (binding.recyclerView.adapter as? ImageAdapter)?.setImages(emptyList())

        Toast.makeText(this, "Images deleted", Toast.LENGTH_SHORT).show()
    }

    private fun simulateNextStep(delayMs: Long) {
        if (downloadedCount >= totalImagesToDownload) {
            tvProgress.text =
                if (totalImagesToDownload < 20) {
                    "Download completed (only $totalImagesToDownload images available)"
                } else {
                    "Download completed"
                }
            return
        }

        // random delay between updates (e.g. 150–400 ms

        simulateRunnable = Runnable {
            val remaining = totalImagesToDownload - downloadedCount
            val step = 1

            downloadedCount += step
            if (downloadedCount > totalImagesToDownload) {
                downloadedCount = totalImagesToDownload
            }

            progressBar.progress = downloadedCount

            if (downloadedCount < totalImagesToDownload) {
                tvProgress.text =
                    if (totalImagesToDownload < 20) {
                        "Downloading $downloadedCount of $totalImagesToDownload... (only $totalImagesToDownload images available)"
                    } else {
                        "Downloading $downloadedCount of $totalImagesToDownload..."
                    }
                // schedule the *next* step
                simulateNextStep(delayMs)
            } else {
                tvProgress.text =
                    if (totalImagesToDownload < 20) {
                        "Download completed (only $totalImagesToDownload images available)"
                    } else {
                        "Download completed"
                    }
            }
        }

        // 🔹 IMPORTANT: schedule the runnable for the *first* time here
        handler.postDelayed(simulateRunnable!!, delayMs)
    }


    private fun cancelSimulatedProgress() {
        simulateRunnable?.let { handler.removeCallbacks(it) }
        simulateRunnable = null
    }


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