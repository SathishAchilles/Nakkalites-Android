package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.domain.models.Banner
import `in`.nakkalites.mediaclient.domain.models.BannerType
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.subscription.PlanUtils
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesVm

class BannerVm(banner: Banner) : BaseModel {
    val id = banner.id
    val name = banner.title
    val type = banner.type
    val thumbnail = when (banner.type) {
        BannerType.VIDEO -> banner.video!!.thumbnailImage
        BannerType.WEB_SERIES -> banner.webSeries!!.thumbnailImage
        else -> null
    }
    val hasPlan =  when (banner.type) {
        BannerType.VIDEO -> banner.video!!.plan != null
        BannerType.WEB_SERIES -> banner.webSeries!!.plan != null
        else -> false
    }
    val planImg: Int? =  when (banner.type) {
        BannerType.VIDEO -> PlanUtils.getPlanIcon(banner.video!!.plan )
        BannerType.WEB_SERIES -> PlanUtils.getPlanIcon(banner.webSeries!!.plan )
        else -> null
    }
    val planColorInt: Int? =  when (banner.type) {
        BannerType.VIDEO -> PlanUtils.getPlanColorInt(banner.video!!.plan?.colorCode )
        BannerType.WEB_SERIES -> PlanUtils.getPlanColorInt(banner.webSeries!!.plan?.colorCode )
        else -> null
    }
    val planName: String? =  when (banner.type) {
        BannerType.VIDEO -> banner.video!!.plan?.name
        BannerType.WEB_SERIES -> banner.webSeries!!.plan?.name
        else -> null
    }
    val videoVm: VideoVm? = banner.video?.let { VideoVm(banner.video) }
    val webSeriesVm: WebSeriesVm? = banner.webSeries?.let { WebSeriesVm(banner.webSeries) }
}
