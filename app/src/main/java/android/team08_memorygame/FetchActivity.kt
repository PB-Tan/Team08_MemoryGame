package android.team08_memorygame


import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.team08_memorygame.databinding.ActivityFetchBinding
import android.view.SoundEffectConstants
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import org.jsoup.Jsoup



class FetchActivity : AppCompatActivity() {


    private lateinit var binding: ActivityFetchBinding

    // for simulated progress
    private val handler = Handler(Looper.getMainLooper())
    private var fetchThread: Thread? = null
    private var simulateRunnable: Runnable? = null
    private var totalImagesToDownload = 20
    private var downloadedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFetchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // grid layout with 4 columns. adapter handles image loading + selection
        binding.apply {
            recyclerView.layoutManager = GridLayoutManager(this@FetchActivity, 4)
            val adapter = ImageAdapter()
            recyclerView.adapter = adapter
        }

        //If this is the second time user is playing
        val fromReplay = intent.getBooleanExtra("FROM_REPLAY", false)
        if (fromReplay) {
            showWelcomePopup()
        }

        // init buttons
        binding.apply {
            fetchButton.setOnClickListener {
                val mp = MediaPlayer.create(this@FetchActivity, R.raw.click_sound)
                mp.setOnCompletionListener { it.release() }
                mp.start()

                val pageUrl = urlField.text.toString()
                if (pageUrl.isNotEmpty()) {
                    fetchImages(pageUrl)
                }
            }

            startButton.setOnClickListener {
                val mp = MediaPlayer.create(this@FetchActivity, R.raw.click_sound)
                mp.setOnCompletionListener { it.release() }
                mp.start()

                val adapter = recyclerView.adapter as? ImageAdapter
                val selected = adapter?.getSelectedImages() ?: emptyList()
                if (selected.size != 6) {
                    Toast.makeText(this@FetchActivity, "please select 6 images", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // Pass the 6 images to PlayActivity
                val intent = Intent(this@FetchActivity, SplashActivity::class.java)
                intent.putStringArrayListExtra("images", ArrayList(selected))
                startActivity(intent)
            }

            deleteButton.setOnClickListener {


                deleteAllImages()
            }
        }
    }

    private fun fetchImages(pageUrl: String) {
        // 1. Interrupt any previous thread and cancel UI progress
        fetchThread?.interrupt()
        cancelSimulatedProgress()

        // 2. Clear grid & reset UI immediately
        (binding.recyclerView.adapter as? ImageAdapter)?.setImages(emptyList())
        binding.progressContainer.visibility = View.GONE

        // 3. Start new download
        fetchThread = Thread {
            try {
                if (Thread.currentThread().isInterrupted) return@Thread

                val urls = fetchImageUrls(pageUrl)
                    .distinct()
                    .filter { it.endsWith(".jpg") || it.endsWith(".png") || it.endsWith(".jpeg") }
                    .take(20)

                if (Thread.currentThread().isInterrupted) return@Thread

                runOnUiThread {
                    if (urls.isEmpty()) {
                        binding.progressContainer.visibility = View.GONE
                        Toast.makeText(this@FetchActivity, "No images found", Toast.LENGTH_SHORT).show()
                    } else {
                        startSimulatedProgress(urls.size, 300)
                        (binding.recyclerView.adapter as? ImageAdapter)?.setImages(urls)
                    }
                }
            } catch (e: InterruptedException) {
                return@Thread
            } catch (e: Exception) {
                if (Thread.currentThread().isInterrupted) return@Thread
                runOnUiThread {
                    binding.progressContainer.visibility = View.GONE
                    Toast.makeText(this, "Parsing failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        fetchThread?.start()
    }

    private fun fetchImageUrls(pageUrl: String): List<String> {
        val imageUrls = mutableListOf<String>()

        try {
            // 1. Connect to webpage
            val doc = Jsoup.connect(pageUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(10_000)
                .get()

            // 2. Grab all <img> tags
            val imgs = doc.select("img")

            for (img in imgs) {
                // Check for interruption inside the loop
                if (Thread.currentThread().isInterrupted) throw InterruptedException()

                // 3. Prioritize common image source attributes
                var src = img.absUrl("src")

                if (src.isEmpty()) {
                    src = img.absUrl("data-src")
                }

                if (src.isEmpty()) {
                    src = img.absUrl("data-lazy")
                }

                // 4. StockSnap special handling
                if (src.contains("cdn.stocksnap.io")) {
                    src = src
                        .replace("/img-thumbs/280h/", "/img-originals/")
                        .replace("/img-thumbs/320h/", "/img-originals/")
                }

                // 5. Basic filtering
                if (src.isNotEmpty() &&
                    (src.endsWith(".jpg") || src.endsWith(".jpeg") || src.endsWith(".png"))
                ) {
                    if (pageUrl.contains("stocksnap.io") && !src.contains("/img-thumbs/")) {
                        continue
                    }
                    imageUrls.add(src)
                }
            }
        } catch (e: Exception) {
            // Rethrow InterruptedException specifically, or print stack trace for others
            if (e is InterruptedException) throw e
            e.printStackTrace()
            // We rethrow logic exceptions so fetchImages catches them
            throw e
        }

        return imageUrls
    }

    private fun startSimulatedProgress(total: Int, delayMs: Long) {
        cancelSimulatedProgress()

        totalImagesToDownload = total
        downloadedCount = 0

        binding.apply {
            progressBar.max = totalImagesToDownload
            progressBar.progress = 0
            tvProgress.text = "Downloading 0 of $totalImagesToDownload..."
            progressContainer.visibility = View.VISIBLE
        }

        simulateNextStep(delayMs)
    }

    private fun deleteAllImages() {
        val dir = filesDir
        dir.listFiles()?.forEach {
            if (it.name.endsWith(".jpg")) {
                it.delete()
            }
        }

        (binding.recyclerView.adapter as? ImageAdapter)?.setImages(emptyList())
        binding.progressContainer.visibility = View.GONE
        Toast.makeText(this, "Images deleted", Toast.LENGTH_SHORT).show()
    }

    private fun simulateNextStep(delayMs: Long) {
        if (downloadedCount >= totalImagesToDownload) {
            binding.tvProgress.text =
                if (totalImagesToDownload < 20) {
                    "Download completed (only $totalImagesToDownload images available)"
                } else {
                    "Download completed"
                }
            return
        }

        simulateRunnable = Runnable {
            val step = 1
            downloadedCount += step
            if (downloadedCount > totalImagesToDownload) {
                downloadedCount = totalImagesToDownload
            }

            binding.progressBar.progress = downloadedCount

            if (downloadedCount < totalImagesToDownload) {
                binding.tvProgress.text =
                    if (totalImagesToDownload < 20) {
                        "Downloading $downloadedCount of $totalImagesToDownload... (only $totalImagesToDownload images available)"
                    } else {
                        "Downloading $downloadedCount of $totalImagesToDownload..."
                    }
                simulateNextStep(delayMs)
            } else {
                binding.tvProgress.text =
                    if (totalImagesToDownload < 20) {
                        "Download completed (only $totalImagesToDownload images available)"
                    } else {
                        "Download completed"
                    }
            }
        }

        handler.postDelayed(simulateRunnable!!, delayMs)
    }

    private fun cancelSimulatedProgress() {
        simulateRunnable?.let { handler.removeCallbacks(it) }
        simulateRunnable = null
    }



    private fun showWelcomePopup() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Welcome back!")
            .setMessage("Ready to start a new game?")
            .setCancelable(false)
            .setIcon(R.drawable.exclamation_dialog)
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel") { _, _ -> finish() }.create()

        dialog.show()
    }
}
