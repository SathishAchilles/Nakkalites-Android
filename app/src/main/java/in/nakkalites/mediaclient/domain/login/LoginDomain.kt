package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.mediaclient.data.user.UserEntity
import `in`.nakkalites.mediaclient.data.user.UserResponse
import `in`.nakkalites.mediaclient.domain.BaseDomain
import `in`.nakkalites.mediaclient.domain.models.User
import android.net.Uri
import com.squareup.moshi.Moshi
import io.reactivex.Single

class LoginDomain(private val userManager: UserManager, val moshi: Moshi) : BaseDomain {

    fun login(
        id: String, displayName: String, email: String, photoUrl: Uri?
    ): Single<User> {
        val json = "{\n" +
                "  \"user\": {\n" +
                "    \"id\": 123,\n" +
                "    \"name\": \"P1\",\n" +
                "    \"email\": \"random@gmail.com\",\n" +
                "    \"image_url\": \"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg/1200px-Jubilee_Tower%2C_north_fa%C3%A7ade_with_entrance.jpg\"\n" +
                "  }\n" +
                "}"
        val jsonAdapter = moshi.adapter(UserResponse::class.java)
        return Single.just(jsonAdapter.fromJson(json))
//        return userManager.login(id, displayName, email, photoUrl)
            .map { UserEntity(id, displayName, email, photoUrl?.toString()) }
            .doOnSuccess { userManager.setUser(it) }
            .map { User(it.id, it.name, it.email, it.imageUrl) }
    }
}
