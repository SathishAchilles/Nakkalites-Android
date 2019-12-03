package `in`.nakkalites.mediaclient.data.videogroup

import `in`.nakkalites.mediaclient.data.HttpConstants
import io.reactivex.Single
import retrofit2.http.GET

interface VideoGroupService {

    @GET(HttpConstants.HOME)
    fun getVideoGroups(): Single<VideoGroupResponse>
}
