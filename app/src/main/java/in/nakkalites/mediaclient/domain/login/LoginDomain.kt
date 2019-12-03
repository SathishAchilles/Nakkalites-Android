package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.mediaclient.data.user.User
import `in`.nakkalites.mediaclient.data.user.UserEntity
import `in`.nakkalites.mediaclient.data.user.UserManager
import `in`.nakkalites.mediaclient.domain.BaseDomain
import io.reactivex.Single

class LoginDomain(private val userManager: UserManager) : BaseDomain {

    fun login(id: String, displayName: String, email: String): Single<User> {
        return userManager.login(id, displayName, email)
            .map { UserEntity(id, displayName, email) }
            .doOnSuccess { userManager.setUser(it) }
            .map { User(it.id, it.name, it.email) }
    }
}
