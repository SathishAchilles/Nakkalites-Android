package `in`.nakkalites.mediaclient.domain.videogroups

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.data.videogroup.VideoGroupService
import `in`.nakkalites.mediaclient.domain.BaseDomain
import `in`.nakkalites.mediaclient.domain.models.Banner
import `in`.nakkalites.mediaclient.domain.models.VideoGroup
import `in`.nakkalites.mediaclient.domain.utils.PagingBody
import io.reactivex.Single

class VideoGroupDomain(private val videoGroupService: VideoGroupService) : BaseDomain {

    fun getAllVideoGroups(pagingBody: PagingBody): Single<Triple<List<Banner>, List<VideoGroup>, String?>> {
        loge(pagingBody.toMap().toString())
        return videoGroupService.getVideoGroups(pagingBody.toMap())
            .map { response ->
                Triple(
                    response.banners.map { entity -> Banner.map(entity) },
                    response.videoGroups.map { entity -> VideoGroup.map(entity) },
                    response.cursor
                )
            }
    }

//    fun getVideoGroup(): Single<List<VideoGroup>> {
//        return videoGroupService.getVideoGroups()
//    }
}
