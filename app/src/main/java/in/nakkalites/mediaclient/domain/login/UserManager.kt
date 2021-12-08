package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.mediaclient.app.constants.AppConstants.DEFAULT_TAG_BG_COLOR
import `in`.nakkalites.mediaclient.data.user.LoginResponse
import `in`.nakkalites.mediaclient.data.user.LoginUserEntity
import `in`.nakkalites.mediaclient.data.user.UserAddEditResponse
import `in`.nakkalites.mediaclient.data.user.UserService
import `in`.nakkalites.mediaclient.domain.models.User
import android.net.Uri
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

class UserManager(private val userService: UserService, private val userDataStore: UserDataStore) {

    private fun setUser(userEntity: LoginUserEntity) {
        setUser(User.map(userEntity))
    }

    private fun setUser(user: User) {
        userDataStore.setUser(user)
    }

    fun setAccessToken(accessToken: String?) {
        userDataStore.setAccessToken(accessToken)
    }

    fun setRefreshToken(refreshToken: String?) {
        userDataStore.setRefreshToken(refreshToken)
    }

    fun loginViaGoogle(
        id: String, displayName: String?, email: String?, photoUrl: Uri?
    ): Single<LoginResponse> {
        val params = mutableMapOf<String, Any>(
            "provider_id" to id,
            "provider_type" to "google"
        ).apply {
            displayName?.let { put("name", displayName) }
            email?.let { put("email", email) }
            photoUrl?.let { put("photo_url", it.toString()) }
        }
        return userService.login(params)
            .doOnSuccess {
                storeUserAndTokens(it)
            }
    }

    fun loginViaTruecaller(
        displayName: String?, email: String?, photoUrl: String?, countryCode: String?,
        phoneNumber: String?, city: String?, gender: String?
    ): Single<LoginResponse> {
        val params = mutableMapOf<String, Any>(
            "provider_type" to "truecaller"
        ).apply {
            displayName?.let { put("name", displayName) }
            email?.let { put("email", email) }
            photoUrl?.let { put("photo_url", it) }
            countryCode?.let { put("country_code", it) }
            phoneNumber?.let { put("mobile", it) }
            city?.let { put("city", it) }
            gender?.let { put("gender", it) }
        }
        return userService.login(params)
            .doOnSuccess {
                storeUserAndTokens(it)
            }
    }

    fun loginViaFirebase(countryCode: String, phoneNumber: String): Single<LoginResponse> {
        val params = mutableMapOf<String, Any>(
            "provider_type" to "firebase",
            "country_code" to countryCode,
            "mobile" to phoneNumber,
        )
        return userService.login(params)
            .doOnSuccess {
                storeUserAndTokens(it)
            }
    }

    fun updateFcmToken(fcmToken: String): Completable {
        userDataStore.setFcmToken(fcmToken)
        val params = mutableMapOf<String, Any>(
            "fcm_token" to fcmToken
        )
        return userService.updateFcmToken(params)
    }

    fun isUserLoggedIn() = userDataStore.getUser() != null

    fun getUser() = userDataStore.getUser()

    fun getRefreshToken() = userDataStore.getRefreshToken()

    fun generateInstanceIdIfNotAvailable() = userDataStore.generateInstanceIdIfNotAvailable()

    fun getInstanceId() = userDataStore.getInstanceIdOrEmpty()

    fun clearAppData() = userDataStore.clearAppData()

    fun isAddProfileShown(): Boolean = userDataStore.isAddProfileShown()

    fun setAddProfileShown() = userDataStore.setAddProfileShown()

    fun isProfileFieldsFilled(): Boolean {
        return getUser()?.let { user ->
            if (user.email.isNullOrEmpty() || user.phoneNumber.isNullOrEmpty()
                || user.countryCode.isNullOrEmpty() || user.name.isNullOrEmpty()
                || user.gender.isNullOrEmpty() || user.dob.isNullOrEmpty()
                || user.city.isNullOrEmpty() || user.country.isNullOrEmpty()
            ) {
                return false
            }
            return true
        } ?: false
    }

    private fun storeUserAndTokens(loginResponse: LoginResponse) {
        setUser(loginResponse.user)
        setAccessToken(loginResponse.user.accessToken)
        setRefreshToken(loginResponse.user.refreshToken)
    }

    fun updateUserProfile(
        name: String?,
        countryCode: String?,
        phoneNumber: String?,
        email: String?,
        gender: String?,
        dob: String?,
        country: String?,
        city: String?
    ): Single<UserAddEditResponse> {
        val params = mutableMapOf<String, Any>().apply {
            name?.apply { put("name", this) }
            email?.apply { put("email", this) }
            if (countryCode != null && phoneNumber != null) {
                put("country_code", countryCode)
                put("mobile", phoneNumber)
            }
            gender?.apply { put("gender", this.toLowerCase(Locale.US)) }
            dob?.apply { put("dob", this) }
            country?.apply { put("country", this) }
            city?.apply { put("city", this) }
        }
        return userService.updateUserProfile(params)
            .doOnSuccess {
                val oldUser = getUser()
                val user = User(
                    it.id, it.name, it.email, oldUser?.imageUrl,
                    oldUser?.providerType, it.countryCode, it.phoneNumber,
                    it.gender, it.dob, it.city, it.country,
                    oldUser?.plan, oldUser?.upgradablePlan, oldUser?.isFirstLogin ?: false
                )
                setUser(user)
                setAccessToken(it.token?.accessToken)
                setRefreshToken(it.token?.refreshToken)
            }
    }

    fun getUserProfile() = userService.getUserProfile()
        .map { User.map(it) }
        .doOnSuccess { setUser(it) }

    fun getCurrentPlansColor(): String = getUser()?.plan?.colorCode ?: DEFAULT_TAG_BG_COLOR
}
