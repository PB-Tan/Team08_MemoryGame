package iss.nus.edu.sg.webviews.memorygame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Player(
    val name: String,
    val timeTaken: Long // in seconds
)

class LeaderboardAdapter(private val playerList: List<Player>) :
    RecyclerView.Adapter<LeaderboardAdapter.PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = playerList[position]

        holder.tvRank.text = "#${position + 1}"
        holder.tvName.text = player.name
        holder.tvTime.text = "${player.timeTaken} sec"

        // Highlight top 3 players with colors
        when (position) {
            0 -> holder.itemView.setBackgroundColor(0xFFFFD700.toInt()) // Gold
            1 -> holder.itemView.setBackgroundColor(0xFFC0C0C0.toInt()) // Silver
            2 -> holder.itemView.setBackgroundColor(0xFFCD7F32.toInt()) // Bronze
            else -> holder.itemView.setBackgroundColor(0xFFFFFFFF.toInt()) // White
        }
    }

    override fun getItemCount(): Int = playerList.size

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }
}
