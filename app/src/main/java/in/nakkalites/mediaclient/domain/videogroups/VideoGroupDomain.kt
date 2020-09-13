package `in`.nakkalites.mediaclient.domain.videogroups

import `in`.nakkalites.mediaclient.data.videogroup.VideoGroupService
import `in`.nakkalites.mediaclient.domain.BaseDomain
import `in`.nakkalites.mediaclient.domain.models.Banner
import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.domain.models.VideoGroup
import `in`.nakkalites.mediaclient.domain.models.WebSeries
import `in`.nakkalites.mediaclient.domain.utils.PagingBody
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Single

class VideoGroupDomain(
    private val videoGroupService: VideoGroupService, private val moshi: Moshi
) : BaseDomain {

    fun getAllVideoGroups(pagingBody: PagingBody): Single<Triple<List<Banner>, List<VideoGroup>, String?>> {
        return videoGroupService.getVideoGroups(pagingBody.toMap())
            .map { response ->
                Triple(
                    response.banners.map { entity -> Banner.map(entity) },
                    response.videoGroups.map { entity -> VideoGroup.map(entity) },
                    response.cursor
                )
            }
    }

    fun getWebSeriesList(pagingBody: PagingBody): Single<Pair<List<WebSeries>, String?>> {
        return videoGroupService.getWebSeriesList(pagingBody.toMap())
            .map { response ->
                Pair(response.webSeriesList.map { WebSeries.map(it) }, response.cursor)
            }
    }

    fun getVideos(
        videoGroupId: String, category: String?, pagingBody: PagingBody
    ): Single<Pair<List<Video>, String?>> {
        val params = mutableMapOf<String, Any>()
            .apply {
                category?.let { put("type", category) }
                putAll(pagingBody.toMap())
            }
        return videoGroupService.getVideosOfVideoGroup(videoGroupId, params)
            .map { response ->
                response.videos.map { videoEntity -> Video.map(videoEntity) } to response.cursor
            }
    }

    fun getWebSeriesDetail(id: String): Single<WebSeries> {
        return videoGroupService.getWebSeriesDetail(id)
            .map { response ->
                WebSeries.map(response.webSeries)
            }
    }

    fun getVideoDetail(id: String): Single<Video> {
        return videoGroupService.getVideoDetail(id)
            .map { response ->
                Video.map(response.video)
            }
    }

    fun trackVideo(id: String, totalDuration: Long, timeElapsed: Long): Completable {
        val params = mutableMapOf<String, Any>(
            "time_duration" to totalDuration,
            "time_elapsed" to timeElapsed
        )
        return videoGroupService.trackVideo(id, params)
    }

    fun getRelatedVideos(
        videoId: String, pagingBody: PagingBody
    ): Single<Pair<List<Video>, String?>> {
        return videoGroupService.getRelatedVideos(videoId, pagingBody.toMap())
            .map { it.videos.map { videoEntity -> Video.map(videoEntity) } to it.cursor }
    }
}
