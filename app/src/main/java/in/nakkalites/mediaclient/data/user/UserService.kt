package `in`.nakkalites.mediaclient.data.user

import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.data.utils.StringAnyMap
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface UserService {
    @Headers(
        "Content-Type:application/json"
    )
    @POST(HttpConstants.LOGIN)
    fun login(@Body params: StringAnyMap): Single<LoginResponse>

    @POST(HttpConstants.TOKEN_REFRESH)
    fun refreshToken(
        @HeaderMap headers: Map<String, String>,
        @Body params: StringAnyMap
    ): Single<RefreshTokenResponse>

    @POST(HttpConstants.FCM_REFRESH)
    fun updateFcmToken(@Body params: StringAnyMap): Completable

    @POST(HttpConstants.USER_PROFILE)
    fun updateUserProfile(@Body params: StringAnyMap): Completable

    @GET(HttpConstants.USER_PROFILE)
    fun getUserProfile(): Single<UserResponse>
}
