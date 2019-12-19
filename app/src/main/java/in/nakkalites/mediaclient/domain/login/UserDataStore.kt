package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.mediaclient.data.PrefsConstants
import `in`.nakkalites.mediaclient.domain.models.User
import android.content.SharedPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi


class UserDataStore(private val prefs: SharedPreferences, private val moshi: Moshi) {

    fun setUser(user: User) {
        val jsonAdapter: JsonAdapter<User> = moshi.adapter(User::class.java)
        prefs.edit().putString(PrefsConstants.USER, jsonAdapter.toJson(user)).apply()
    }

    fun getUser(): User? {
//        return User("123", "Pavan", "thynameisp1@gmail.com")
        return try {
            val userJson = prefs.getString(PrefsConstants.USER, null) ?: return null
            val jsonAdapter: JsonAdapter<User> = moshi.adapter(User::class.java)
            jsonAdapter.fromJson(userJson)
        } catch (e: Exception) {
            null
        }
    }

    fun setAccessToken(accessToken: String) {
        prefs.edit().putString(PrefsConstants.ACCESS_TOKEN, accessToken).apply()
    }

    fun getAccessToken(): String? = prefs.getString(PrefsConstants.ACCESS_TOKEN, null)
}
