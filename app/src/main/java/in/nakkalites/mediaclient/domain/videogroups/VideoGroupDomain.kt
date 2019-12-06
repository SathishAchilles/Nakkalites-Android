package `in`.nakkalites.mediaclient.domain.videogroups

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.data.videogroup.VideoGroupService
import `in`.nakkalites.mediaclient.domain.BaseDomain
import `in`.nakkalites.mediaclient.domain.models.Banner
import `in`.nakkalites.mediaclient.domain.models.VideoGroup
import `in`.nakkalites.mediaclient.domain.models.WebSeries
import `in`.nakkalites.mediaclient.domain.utils.PagingBody
import io.reactivex.Single
import timber.log.Timber

class VideoGroupDomain(private val videoGroupService: VideoGroupService) : BaseDomain {

    fun getAllVideoGroups(pagingBody: PagingBody): Single<Triple<List<Banner>, List<VideoGroup>, String?>> {
        loge(pagingBody.toMap().toString())
        return videoGroupService.getVideoGroups(pagingBody.toMap())
            .map { response ->
                Timber.e(response.toString())
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
                Timber.e(response.toString())
                Pair(response.webSeriesList.map { WebSeries.map(it) }, response.cursor)
            }
    }
}
