package android.team08_memorygame

object UserManager {
    var userIsPremium: Boolean = false
    val userId:String = "User_123"

    fun setPremiumStatus(isPremium: Boolean){
        userIsPremium = isPremium
    }
}