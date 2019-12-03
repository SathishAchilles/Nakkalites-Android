package `in`.nakkalites.mediaclient.data.user

import android.net.Uri
import io.reactivex.Single

class UserManager(private val userService: UserService, private val userDataStore: UserDataStore) {

    fun setUser(userEntity: UserEntity) {
        val user = User(userEntity.id, userEntity.name, userEntity.email)
        userDataStore.setUser(user)
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
