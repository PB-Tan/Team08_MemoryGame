import android.team08_memorygame.R
import android.team08_memorygame.Score
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private val scores: List<Score>) :
    RecyclerView.Adapter<LeaderboardAdapter.ScoreViewHolder>() {

    class ScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankText: TextView = view.findViewById(R.id.rankText)
        val nameText: TextView = view.findViewById(R.id.nameText)
        val scoreText: TextView = view.findViewById(R.id.scoreText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ScoreViewHolder(view)
    }

    private fun formatTime(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return "%d:%02d".format(m, s)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scores[position]
        holder.rankText.text = "${position + 1}"
        holder.nameText.text = score.name
        holder.scoreText.text = formatTime(score.score)

        // Odd / Even row coloring for the leaderboard list view
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(
                holder.itemView.context.getColor(R.color.row_even)
            )
        } else {
            holder.itemView.setBackgroundColor(
                holder.itemView.context.getColor(R.color.row_odd)
            )
        }
    }

    override fun getItemCount(): Int = scores.size
}
