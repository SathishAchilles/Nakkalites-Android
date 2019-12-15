package `in`.nakkalites.mediaclient.data.user

import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.data.utils.StringAnyMap
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {
    @POST(HttpConstants.LOGIN)
    fun login(@Body params: StringAnyMap): Single<UserResponse>
}
