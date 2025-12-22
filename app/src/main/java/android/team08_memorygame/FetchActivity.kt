package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.team08_memorygame.databinding.ActivityFetchBinding
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

                val intent = Intent(this@FetchActivity, SelectedPreviewActivity::class.java)
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