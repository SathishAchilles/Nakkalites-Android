package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.mediaclient.domain.BaseDomain
import `in`.nakkalites.mediaclient.domain.models.User
import android.net.Uri
import com.squareup.moshi.Moshi
import io.reactivex.Single

class LoginDomain(private val userManager: UserManager, val moshi: Moshi) : BaseDomain {

    fun loginViaGoogle(
        id: String, displayName: String?, email: String, photoUrl: Uri?
    ): Single<User> {
        return userManager.loginViaGoogle(id, displayName, email, photoUrl)
            .map { it.user }
            .map {
                User(
                    it.id, it.name, it.email, it.imageUrl, it.providerType, it.countryCode,
                    it.country, it.gender, it.dob, it.city, it.country
                )
            }
    }

    fun loginViaFirebaseOtp(countryCode: String, phoneNumber: String): Single<User> {
        return userManager.loginViaFirebase(countryCode, phoneNumber)
            .map { it.user }
            .map {
                User(it.id, it.name, it.email, it.imageUrl, it.providerType, it.countryCode,
                    it.country, it.gender, it.dob, it.city, it.country)
            }
    }
}
