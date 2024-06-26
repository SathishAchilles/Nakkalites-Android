package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.mediaclient.data.PrefsConstants
import `in`.nakkalites.mediaclient.data.utils.generateUnhyphenatedUuid
import `in`.nakkalites.mediaclient.data.utils.getStringOrEmpty
import `in`.nakkalites.mediaclient.domain.models.User
import android.content.SharedPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import timber.log.Timber


class UserDataStore(private val prefs: SharedPreferences, private val moshi: Moshi) {

    init {
        generateInstanceIdIfNotAvailable()
    }

    fun setUser(user: User) {
        val jsonAdapter: JsonAdapter<User> = moshi.adapter(User::class.java)
        prefs.edit().putString(PrefsConstants.USER, jsonAdapter.toJson(user)).apply()
    }

    fun getUser(): User? {
        return try {
            val userJson = prefs.getString(PrefsConstants.USER, null) ?: return null
            val jsonAdapter: JsonAdapter<User> = moshi.adapter(User::class.java)
            jsonAdapter.fromJson(userJson)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    fun setAccessToken(accessToken: String?) {
        accessToken?.let {
            prefs.edit().putString(PrefsConstants.ACCESS_TOKEN, "Token $it").apply()
        }
    }

    fun getAccessToken(): String = prefs.getStringOrEmpty(PrefsConstants.ACCESS_TOKEN)

    fun setRefreshToken(refreshToken: String?) {
        refreshToken?.let {
            prefs.edit().putString(PrefsConstants.REFRESH_TOKEN, it).apply()
        }
    }

    fun getRefreshToken(): String = prefs.getStringOrEmpty(PrefsConstants.REFRESH_TOKEN)

    fun setFcmToken(fcmToken: String?) {
        fcmToken?.let {
            prefs.edit().putString(PrefsConstants.FCM_TOKEN, it).apply()
        }
    }

    fun isAddProfileShown(): Boolean = prefs.getBoolean(PrefsConstants.IS_ADD_PROFILE_SHOWN, false)

    fun setAddProfileShown() {
        prefs.edit().putBoolean(PrefsConstants.IS_ADD_PROFILE_SHOWN, true).apply()
    }

    fun generateInstanceIdIfNotAvailable() {
        if (!isInstanceIdAvailable()) {
            prefs.edit().putString(PrefsConstants.INSTANCE_ID, generateUnhyphenatedUuid()).apply()
        }
    }

    fun getInstanceIdOrEmpty() = prefs.getStringOrEmpty(PrefsConstants.INSTANCE_ID)

    private fun isInstanceIdAvailable() =
        prefs.contains(PrefsConstants.INSTANCE_ID) && getInstanceIdOrEmpty().isNotEmpty()

    fun clearAppData() = prefs.edit().clear().apply()
}
