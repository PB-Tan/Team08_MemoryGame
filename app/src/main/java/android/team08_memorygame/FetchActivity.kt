package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jsoup.Jsoup
import kotlin.random.Random

class FetchActivity : AppCompatActivity() {

    private lateinit var etUrl: EditText
    private lateinit var btnFetch: Button
    private lateinit var btnPlay: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter

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
        setContentView(R.layout.activity_fetch)

        etUrl = findViewById(R.id.etUrl)
        btnFetch = findViewById(R.id.btnFetch)
        btnPlay = findViewById(R.id.btnPlay)
        recyclerView = findViewById(R.id.recyclerView)

        progressContainer = findViewById(R.id.progressContainer)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)

        // grid layout with 4 columns. adapter handles image loading + selection
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        adapter = ImageAdapter()
        recyclerView.adapter = adapter

        btnFetch.setOnClickListener {
            val pageUrl = etUrl.text.toString()
            if (pageUrl.isNotEmpty()) {
                fetchImages(pageUrl)
            }
        }

        btnPlay.setOnClickListener {
            val selected = adapter.getSelectedImages()
            if (selected.size != 6) {
                Toast.makeText(this, "please select 6 images", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //navigate to next screen, sends selected image urls to selectedpreviewactivity
            val intent = Intent(this, SelectedPreviewActivity::class.java)
            intent.putStringArrayListExtra("images", ArrayList(selected)) //attach the selected urls under the key "images"
            startActivity(intent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        cancelSimulatedProgress()
    }
    private fun fetchImages(pageUrl: String) {
        Thread {
            try {
                val urls = fetchImageUrls(pageUrl) //returns a big list
                    .distinct() // remove dupes
                    //.filter { it.endsWith(".jpg") || it.endsWith(".png") } //taking out cuz previously alr filtered
                    .take(20) //limit to 20 image
                val delayMs: Long = 200
                Thread.sleep(delayMs)
                runOnUiThread {
                    if (urls.isEmpty()) {

                        //clear old images if no results found for new url
                        adapter.setImages(emptyList())
                        // nothing to download, hide progress and warn user
                        progressContainer.visibility = View.GONE
                        Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show()
                    } else {
                        // 🔹 NEW: start simulated progress based on how many URLs we actually have
                        startSimulatedProgress(urls.size, delayMs)
                        adapter.setImages(urls) // urls copied into adapter
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    // 🔹 NEW: stop and hide progress bar on error
                    cancelSimulatedProgress()
                    progressContainer.visibility = View.GONE
                    Toast.makeText(this, "error - please check input URL", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }


    // this is the part that fetches the ImageURLs strings
    private fun fetchImageUrls(pageUrl: String): List<String> {
        val imageUrls = mutableListOf<String>()

        // 1. 连接网页（伪装成浏览器） --- connect to webpage
        val doc = Jsoup.connect(pageUrl)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .timeout(10_000)
            .get()

        // 2. 抓取所有 <img> 标签 --- find all <img> tags
        val imgs = doc.select("img")

        for (img in imgs) {

            // 3. 优先尝试常见的图片来源属性 --- extract image urls. checks for normal images, lazy-loaded images (load only when needed)
            var src = img.absUrl("src")

            if (src.isEmpty()) {
                src = img.absUrl("data-src")
            }

            if (src.isEmpty()) {
                src = img.absUrl("data-lazy")
            }

            // 4. StockSnap 特殊处理： --- workaround for stocksnap(cuz it uses thumbnail urls, and it upgrades the thumbnail to original image)
            //    把缩略图地址替换为原图地址（避免 CDN 403）
//            if (src.contains("cdn.stocksnap.io")) {
//                src = src
//                    .replace("/img-thumbs/280h/", "/img-originals/")
//                    .replace("/img-thumbs/320h/", "/img-originals/")
//            }

            // 5. 基本过滤 --- filter valid images
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


