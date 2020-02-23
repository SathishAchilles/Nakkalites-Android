package `in`.nakkalites.mediaclient.data.videogroup

import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.data.utils.StringAnyMap
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface VideoGroupService {
    @GET(HttpConstants.VIDEO_GROUPS)
    fun getVideoGroups(@QueryMap paging: StringAnyMap): Single<VideoGroupsResponse>

    @GET(HttpConstants.WEBSERIES)
    fun getWebSeriesList(@QueryMap paging: StringAnyMap): Single<WebSeriesListResponse>

    @GET(HttpConstants.VIDEO_GROUP_DETAIL)
    fun getVideosOfVideoGroup(
        @Path("video-group-id") id: String, @QueryMap paging: StringAnyMap
    ): Single<VideosResponse>

    @GET(HttpConstants.WEBSERIES_DETAIL)
    fun getWebSeriesDetail(@Path("webseries-id") id: String): Single<WebSeriesDetailResponse>

    @GET(HttpConstants.VIDEO_DETAIL)
    fun getVideoDetail(@Path("video-id") id: String): Single<VideoDetailResponse>

    @GET(HttpConstants.VIDEO_RELATED)
    fun getRelatedVideos(
        @Path("video-id") id: String, @QueryMap paging: StringAnyMap
    ): Single<VideosResponse>

    @POST(HttpConstants.VIDEO_TRACK)
    @Headers(
        "Content-Type:application/json"
    )
    fun trackVideo(
        @Path("video-id") id: String, @Body params: StringAnyMap
    ): Completable
}
