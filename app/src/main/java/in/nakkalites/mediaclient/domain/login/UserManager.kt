package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.mediaclient.data.user.LoginResponse
import `in`.nakkalites.mediaclient.data.user.RefreshTokenResponse
import `in`.nakkalites.mediaclient.data.user.UserEntity
import `in`.nakkalites.mediaclient.data.user.UserService
import `in`.nakkalites.mediaclient.domain.models.User
import android.net.Uri
import io.reactivex.Completable
import io.reactivex.Single

class UserManager(private val userService: UserService, private val userDataStore: UserDataStore) {

    private fun setUser(userEntity: UserEntity) {
        userDataStore.setUser(User.map(userEntity))
    }

    fun setAccessToken(accessToken: String?) {
        userDataStore.setAccessToken(accessToken)
    }

    fun setRefreshToken(refreshToken: String?) {
        userDataStore.setRefreshToken(refreshToken)
    }

    private fun setFcmToken(fcmToken: String?) {
        userDataStore.setFcmToken(fcmToken)
    }

    fun login(
        type: String, id: String, displayName: String?, email: String?, photoUrl: Uri?
    ): Single<LoginResponse> {
        val params = mutableMapOf<String, Any>(
            "id" to id,
            "type" to type
        ).apply {
            displayName?.let { put("name", displayName) }
            email?.let { put("email", email) }
            photoUrl?.let { put("photo_url", it.toString()) }
        }
//        return Single.just(LoginResponse(UserEntity("123", "Pavan", "thynameisp1@gmail.com", null, ""))
        return userService.login(params)
            .doOnSuccess {
                setUser(it.user)
                setAccessToken(it.user.accessToken)
                setRefreshToken(it.user.refreshToken)
            }
    }

    fun refreshToken(
        headers: Map<String, String>,
        refreshToken: String
    ): Single<RefreshTokenResponse> {
        val params = mutableMapOf<String, Any>(
            "refresh_token" to refreshToken
        )
        return userService.refreshToken(headers, params)
            .doOnSuccess {
                setAccessToken(it.accessToken)
                setRefreshToken(it.accessToken)
            }
    }

    fun updateFcmToken(fcmToken: String): Completable {
        val params = mutableMapOf<String, Any>(
            "fcm_token" to fcmToken
        )
        return userService.updateFcmToken(params)
    }

    fun isUserLoggedIn() = userDataStore.getUser() != null

    fun getUser() = userDataStore.getUser()

    fun getRefreshToken() = userDataStore.getRefreshToken()

    fun clearAppData() = userDataStore.clearAppData()
}
