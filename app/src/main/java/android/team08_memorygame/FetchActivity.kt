package android.team08_memorygame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


class FetchActivity : AppCompatActivity() {

    private lateinit var urlField: EditText
    private lateinit var fetchBtn: Button
    private lateinit var playBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fetch)

        fetchBtn = findViewById<Button>(R.id.fetch_button)
        deleteBtn = findViewById<Button>(R.id.delete_button)
        playBtn = findViewById<Button>(R.id.start_button)
        urlField = findViewById<EditText>(R.id.url_field)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = ImageAdapter()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //If this is the second time user is playing
        val fromReplay = intent.getBooleanExtra("FROM_REPLAY", false)
        if (fromReplay) {
            showWelcomePopup()
        }

        fetchBtn.setOnClickListener {
            val pageUrl = urlField.text.toString()
            if (pageUrl.isNotEmpty()) {
                fetchImages(pageUrl)
            }
        }

        playBtn.setOnClickListener {
            val selected = adapter.getSelectedImages()
            if (selected.size != 6) {
                Toast.makeText(this, "please select 6 images", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

        deleteBtn.setOnClickListener {
            deleteAllImages(imgView)
        }

        val intent = Intent(this, SelectedPreviewActivity::class.java)
        intent.putStringArrayListExtra("images", ArrayList(selected))
        startActivity(intent)
    }

        private fun fetchImages(pageUrl: String) {
            Thread {
                try {
                    val urls = fetchImageUrls(pageUrl)
                        .distinct()
                        .filter { it.endsWith(".jpg") || it.endsWith(".png") }
                        .take(20)

                    runOnUiThread {
                        adapter.setImages(urls)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "解析失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        private fun fetchImageUrls(pageUrl: String): List<String> {
            val imageUrls = mutableListOf<String>()

            // 1. 连接网页（伪装成浏览器）
            val doc = Jsoup.connect(pageUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(10_000)
                .get()

            // 2. 抓取所有 <img> 标签
            val imgs = doc.select("img")

            for (img in imgs) {

                // 3. 优先尝试常见的图片来源属性
                var src = img.absUrl("src")

                if (src.isEmpty()) {
                    src = img.absUrl("data-src")
                }

                if (src.isEmpty()) {
                    src = img.absUrl("data-lazy")
                }

                // 4. StockSnap 特殊处理：
                //    把缩略图地址替换为原图地址（避免 CDN 403）
                if (src.contains("cdn.stocksnap.io")) {
                    src = src
                        .replace("/img-thumbs/280h/", "/img-originals/")
                        .replace("/img-thumbs/320h/", "/img-originals/")
                }

                // 5. 基本过滤
                if (
                    src.isNotEmpty() &&
                    (src.endsWith(".jpg") || src.endsWith(".jpeg") || src.endsWith(".png"))
                ) {
                    imageUrls.add(src)
                }
            }

            return imageUrls
        }

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