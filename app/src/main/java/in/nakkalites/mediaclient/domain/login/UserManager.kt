package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.data.user.UserEntity
import `in`.nakkalites.mediaclient.data.user.UserResponse
import `in`.nakkalites.mediaclient.data.user.UserService
import `in`.nakkalites.mediaclient.domain.models.User
import android.net.Uri
import io.reactivex.Single

class UserManager(private val userService: UserService, private val userDataStore: UserDataStore) {

    fun setUser(userEntity: UserEntity) {
        userDataStore.setUser(User.map(userEntity))
    }

    fun login(
        id: String, displayName: String, email: String, photoUrl: Uri?
    ): Single<UserResponse> {
//        return Single.just(UserResponse(UserEntity("123", "Pavan", "thynameisp1@gmail.com")))
        val params = mutableMapOf<String, Any>(
            "id" to id,
            "name" to displayName,
            "email" to email
        ).apply {
            photoUrl?.let { put("photo_url", it.toString()) }
        }
        loge("params $params")
        return userService.login(params)
            .doOnSuccess { setUser(it.user) }
    }

    fun isUserLoggedIn() = userDataStore.getUser() != null

    fun getUser() = userDataStore.getUser()
}
