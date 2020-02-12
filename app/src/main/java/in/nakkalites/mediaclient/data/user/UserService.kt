package `in`.nakkalites.mediaclient.data.user

import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.data.utils.StringAnyMap
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {
    @POST("https://sleepy-lowlands-51142.herokuapp.com/api/" + HttpConstants.LOGIN)
    fun login(@Body params: StringAnyMap): Single<LoginResponse>
}
