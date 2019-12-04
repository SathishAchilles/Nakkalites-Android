package `in`.nakkalites.mediaclient.data.videogroup

import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.data.utils.StringAnyMap
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.QueryMap

interface VideoGroupService {
    @GET(HttpConstants.HOME)
    @Headers("Content-Type:application/json", "x-api-key:PMAK-5de80ebba02a48003620e12a-3bf8efa50246a6a6b168c9f5f85cda46bb")
    fun getVideoGroups( @QueryMap paging: StringAnyMap): Single<VideoGroupResponse>
}
