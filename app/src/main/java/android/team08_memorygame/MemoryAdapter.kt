package android.team08_memorygame

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView


data class Card(val imageId: Int, var isFaceUp: Boolean = false, var isMatched: Boolean = false)


class MemoryAdapter(private val context: Context, private val cards: List<Card>) : BaseAdapter() {

    override fun getCount(): Int {
        return cards.size
    }

    override fun getItem(position: Int): Any {
        return cards[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView
        if (convertView == null) {
            imageView = ImageView(context)
            //resize
            imageView.layoutParams = AbsListView.LayoutParams(300, 300)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(8, 8, 8, 8)
        } else {
            imageView = convertView as ImageView
        }

        val card = cards[position]

        // display pictures
        if (card.isFaceUp || card.isMatched) {
            imageView.setImageResource(card.imageId)
        } else {
            // if imageview=null  set gray
            imageView.setImageResource(0)
            imageView.setBackgroundColor(context.getColor(android.R.color.darker_gray))
        }
        return imageView
    }
}