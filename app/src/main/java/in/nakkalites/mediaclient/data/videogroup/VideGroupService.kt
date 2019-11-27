package `in`.nakkalites.mediaclient.data.videogroup

import `in`.nakkalites.mediaclient.app.constants.HttpConstants
import io.reactivex.Single
import retrofit2.http.GET

interface VideGroupService {

    @GET(HttpConstants.HOME)
    fun getVideoGroups(): Single<VideoGroupResponse>
}
