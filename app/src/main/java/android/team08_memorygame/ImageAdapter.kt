package android.team08_memorygame

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders

class ImageAdapter() : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {


    private val images = mutableListOf<String>()
    private val selected = mutableSetOf<String>()



    fun glideUrlWithUA(url: String): GlideUrl {
        return GlideUrl(
            url,
            LazyHeaders.Builder()
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.itemImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = images[position]

        // each grid tile downloads/displays the image via Glide
        Glide.with(holder.image.context)
            .load(glideUrlWithUA(url))
            .into(holder.image)



        holder.image.alpha = if (selected.contains(url)) 0.5f else 1f // if selected -> semi-transparent

        holder.image.setOnClickListener {
            if (selected.contains(url)) {  // unselect if already selected
                selected.remove(url)
                holder.image.alpha = 1f
            } else if (selected.size < 6) {
                selected.add(url)
                holder.image.alpha = 0.5f
            }
        }
    }

    fun setImages(newImages: List<String>) {
        images.clear()
        selected.clear()
        notifyDataSetChanged()

        // Start a background thread to add images one by one
        Thread {
            for (url in newImages) {
                try {
                    Thread.sleep(200)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Add the image on the Main UI Thread
                Handler(Looper.getMainLooper()).post {
                    images.add(url)
                    // Notify that a new item was inserted at the end of the list
                    notifyItemInserted(images.size - 1)
                }
            }
        }.start()
    }

    fun getSelectedImages(): List<String> = selected.toList()
}
