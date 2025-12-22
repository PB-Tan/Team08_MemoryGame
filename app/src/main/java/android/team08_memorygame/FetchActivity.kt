package android.team08_memorygame

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jsoup.Jsoup
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target

class FetchActivity : AppCompatActivity() {

    private lateinit var etUrl: EditText
    private lateinit var btnFetch: Button
    private lateinit var btnPlay: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter

    private lateinit var progressContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
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

        // grid layout with 3 columns. adapter handles image loading + selection
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = ImageAdapter()
        recyclerView.adapter = adapter

        btnFetch.setOnClickListener {
            val pageUrl = etUrl.text.toString()
            if (pageUrl.isNotEmpty()) {
                startRealTimeProgress()
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

    private fun fetchImages(pageUrl: String) {
        Thread {
            try {
                val urls = fetchImageUrls(pageUrl)//returns a big list
                    .distinct() // remove dupes
                    .filter { it.endsWith(".jpg") || it.endsWith(".png") } //keeps jpg/png only
                    .take(20) //limit to 20 image

                runOnUiThread {
                    if (urls.isEmpty()) {
                        progressContainer.visibility = View.GONE
                        Toast.makeText(this, "no images found", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    // set progress based on the REAL number of URLs (could be < 20)
                    totalImagesToDownload = urls.size
                    downloadedCount = 0

                    progressBar.max = totalImagesToDownload
                    progressBar.progress = 0
                    tvProgress.text = "Downloading 0 of $totalImagesToDownload..."

                    // start downloading all images into Glide's cache
                    preloadImages(urls)

                    adapter.setImages(urls) // urls copied into adapter
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "解析失败 network error?", Toast.LENGTH_SHORT).show()
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
            if (src.contains("cdn.stocksnap.io")) {
                src = src
                    .replace("/img-thumbs/280h/", "/img-originals/")
                    .replace("/img-thumbs/320h/", "/img-originals/")
            }

            // 5. 基本过滤 --- filter valid images
            if (
                src.isNotEmpty() &&
                (src.endsWith(".jpg") || src.endsWith(".jpeg") || src.endsWith(".png"))
            ) {
                imageUrls.add(src)
            }
        }

        return imageUrls
    }

    // called when user starts a new fetch
    private fun startRealTimeProgress() {
        downloadedCount = 0
        totalImagesToDownload = 20   // or adapt to actual size later
        progressBar.max = totalImagesToDownload
        progressBar.progress = 0
        tvProgress.text = "Downloading 0 of $totalImagesToDownload..."
        progressContainer.visibility = View.VISIBLE
    }

    // called by adapter whenever an image finishes loading
    private fun preloadImages(urls: List<String>) {
        for (url in urls) {
            Glide.with(this)
                .load(glideUrlWithUA(url))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        onSingleImageFinished()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        onSingleImageFinished()
                        return false
                    }
                })
                .preload()
        }
    }


    // one image (success or fail) has finished downloading
    private fun onSingleImageFinished() {
        downloadedCount++

        runOnUiThread {
            if (downloadedCount > totalImagesToDownload) {
                downloadedCount = totalImagesToDownload
            }

            progressBar.progress = downloadedCount

            if (downloadedCount < totalImagesToDownload) {
                tvProgress.text = "Downloading $downloadedCount of $totalImagesToDownload..."
            } else {
                tvProgress.text = "Download completed"
            }
        }
    }

    // helper to add User-Agent header, same idea as in adapter
    private fun glideUrlWithUA(url: String): GlideUrl {
        return GlideUrl(
            url,
            LazyHeaders.Builder()
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()
        )
    }
}


