package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jsoup.Jsoup

class FetchActivity : AppCompatActivity() {

    private lateinit var etUrl: EditText
    private lateinit var btnFetch: Button
    private lateinit var btnPlay: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetch)

        etUrl = findViewById(R.id.etUrl)
        btnFetch = findViewById(R.id.btnFetch)
        btnPlay = findViewById(R.id.btnPlay)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = GridLayoutManager(this, 3)
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
                Toast.makeText(this, "请选择 6 张图片", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, PlayActivity::class.java)
            intent.putStringArrayListExtra("images", ArrayList(selected))
            startActivity(intent)
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
