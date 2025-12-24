package iss.nus.edu.sg.webviews.memorygame.network



// ---------- AUTH ----------
data class UserRequest(
    val username: String,
    val password: String
)

data class UserResponse(
    val userId: Int,
    val message: String
)

// ---------- GAME ----------
data class GameResultRequest(
    val username: String,
    val timeTakenSeconds: Int
)
