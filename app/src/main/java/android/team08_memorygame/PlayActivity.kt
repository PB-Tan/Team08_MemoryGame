package android.team08_memorygame

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PlayActivity : AppCompatActivity() {
    private var cardList = mutableListOf<Card>()
    private lateinit var adapter: MemoryAdapter

    // game
    private var firstSelectedPosition: Int = -1
    private var isBusy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //score_button
        val leaderboardBtn = findViewById<Button>(R.id.leader_button)
        leaderboardBtn.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        val backButton = findViewById<ImageButton>(R.id.back)
        backButton.setOnClickListener {
            val intent = Intent(this, FetchActivity::class.java)
            startActivity(intent)
        }

        setupGame()
    }

    private fun setupGame() {
        val intentImages = intent.getStringArrayListExtra("images")
        
        if (intentImages == null || intentImages.size != 6) {
            Toast.makeText(this, "Game requires 6 images from selection", Toast.LENGTH_LONG).show()
            return
        }
        
        val images = intentImages.toList()

        //copy pictures
        val allImages = (images + images).shuffled()
        //put pics in a container
        cardList.clear()
        for (img in allImages) {
            cardList.add(Card(img))
        }

        // 3. bind the Adapter
        val gridView = findViewById<GridView>(R.id.gridView)
        adapter = MemoryAdapter(this, cardList)
        gridView.adapter = adapter
        
        gridView.setOnItemClickListener { _, _, position, _ ->
            onCardClicked(position)
        }
    }

    private fun onCardClicked(position: Int) {

        val currentCard = cardList[position]

        // no return type so return means over
        if (isBusy || currentCard.isFaceUp || currentCard.isMatched) return


        currentCard.isFaceUp = true
        adapter.notifyDataSetChanged() // refresh UI

        //choose pic
        if (firstSelectedPosition == -1) {
            // choose first one
            firstSelectedPosition = position
        } else {//already choose one pic

            val firstCard = cardList[firstSelectedPosition]

            if (firstCard.imageUrl == currentCard.imageUrl) {

                firstCard.isMatched = true
                currentCard.isMatched = true
                firstSelectedPosition = -1//clear

                checkWin()
            } else {
                // don't match
                isBusy = true // avoid click other places

                // postDelayed
                Handler(Looper.getMainLooper()).postDelayed({
                    firstCard.isFaceUp = false
                    currentCard.isFaceUp = false
                    firstSelectedPosition = -1
                    isBusy = false
                    adapter.notifyDataSetChanged() // refresh close pic
                }, 1000)
            }
        }
    }

    private fun checkWin() {
        if (cardList.all { it.isMatched }) {
            Toast.makeText(this, "Congratulations!", Toast.LENGTH_SHORT).show()
        }
    }
}
