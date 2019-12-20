package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.data.user.UserEntity
import `in`.nakkalites.mediaclient.data.user.LoginResponse
import `in`.nakkalites.mediaclient.data.user.UserService
import `in`.nakkalites.mediaclient.domain.models.User
import android.net.Uri
import io.reactivex.Single

class UserManager(private val userService: UserService, private val userDataStore: UserDataStore) {

    private fun setUser(userEntity: UserEntity) {
        userDataStore.setUser(User.map(userEntity))
    }

    private fun setAccessToken(accessToken: String?) {
        userDataStore.setAccessToken(accessToken)
    }

    fun login(
        id: String, displayName: String?, email: String?, photoUrl: Uri?
    ): Single<LoginResponse> {
//        return Single.just(UserResponse(UserEntity("123", "Pavan", "thynameisp1@gmail.com")))
        val params = mutableMapOf<String, Any>(
            "id" to id
        ).apply {
            displayName?.let { put("name", displayName) }
            email?.let { put("email", email) }
            photoUrl?.let { put("photo_url", it.toString()) }
        }
        loge("params $params $id $displayName $email $photoUrl")
        return userService.login(params)
            .doOnSuccess {
                setUser(it.user)
                setAccessToken(it.accessToken)
            }
    }

    fun isUserLoggedIn() = userDataStore.getUser() != null

    fun getUser() = userDataStore.getUser()
}
