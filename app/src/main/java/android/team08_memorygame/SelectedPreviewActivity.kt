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

        val images = intent.getStringArrayListExtra("images")

        if (images == null || images.isEmpty()) {
            Toast.makeText(this, "No images received", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val container = findViewById<LinearLayout>(R.id.imageContainer)

        // 一个一个 ImageView 加进去
        for (url in images) {
            val iv = ImageView(this)
            iv.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            )
            iv.scaleType = ImageView.ScaleType.CENTER_CROP

            iv.load(url)

            container.addView(iv)
        }
    }
}
