package `in`.nakkalites.mediaclient.domain.login

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
        return userService.login()
            .doOnSuccess { setUser(it.user) }
    }

    fun isUserLoggedIn() = userDataStore.getUser() != null

    fun getUser() = userDataStore.getUser()
}
