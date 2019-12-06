package `in`.nakkalites.mediaclient.data.videogroup

import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.data.utils.StringAnyMap
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface VideoGroupService {
    @GET(HttpConstants.VIDEO_GROUPS)
    @Headers(
        "Content-Type:application/json",
        "x-api-key:PMAK-5de80ebba02a48003620e12a-5dd8c491cd75f1ffbf9c7c4fb32270fee6"
    )
    fun getVideoGroups(@QueryMap paging: StringAnyMap): Single<VideoGroupResponse>

    @GET(HttpConstants.WEBSERIES)
    fun getWebSeriesList(@QueryMap paging: StringAnyMap): Single<WebSeriesListResponse>

    @GET(HttpConstants.VIDEO_GROUP)
    fun getVideosOfVideoGroup(
        @Path("video-group-id") id: String, @QueryMap paging: StringAnyMap
    ): Single<VideosResponse>
}
