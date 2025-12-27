package android.team08_memorygame

object UserManager {
    var userIsPremium: Boolean = false
    var userId:String? = null

    fun setPremiumStatus(isPremium: Boolean){
        userIsPremium = isPremium
    }
}