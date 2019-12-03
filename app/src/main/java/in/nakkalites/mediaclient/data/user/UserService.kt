package `in`.nakkalites.mediaclient.data.user

import `in`.nakkalites.mediaclient.data.HttpConstants
import io.reactivex.Single
import retrofit2.http.POST

interface UserService {
    @POST(HttpConstants.LOGIN)
    fun login(): Single<UserResponse>
}
