package android.team08_memorygame.network
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MemoryGameApi {

    // -------- AUTH --------
    @POST("api/Users/signin")
    fun signIn(
        @Body request: UserRequest
    ): Call<UserResponse>

    // -------- GAME RESULT --------
    @POST("api/GameResults")
    fun saveGameResult(
        @Body request: GameResultRequest
    ): Call<Void>
}
