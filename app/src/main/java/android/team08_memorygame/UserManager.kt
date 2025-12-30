package android.team08_memorygame

object UserManager {
    var userIsPremium: Boolean = false
    var username:String? = null

    fun setPremiumStatus(isPremium: Boolean){
        userIsPremium = isPremium
    }
}