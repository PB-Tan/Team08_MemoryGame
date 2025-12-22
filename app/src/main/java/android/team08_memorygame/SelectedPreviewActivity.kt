package android.team08_memorygame

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load

class SelectedPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_preview)

        //receiving the list from intent
        val images = intent.getStringArrayListExtra("images")

        // prevents crashes if something goes wrong
        if (images == null || images.isEmpty()) {
            Toast.makeText(this, "No images received", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // vertical layout that images will be added to
        val container = findViewById<LinearLayout>(R.id.imageContainer)

        // for each url, the images are loaded here via coil
        // 一个一个 ImageView 加进去
        for (url in images) {
            val iv = ImageView(this)
            iv.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            )
            iv.scaleType = ImageView.ScaleType.CENTER_CROP

            // using coil - another image loading library (lighter than glide)
            iv.load(url)

            container.addView(iv)
        }
    }
}
