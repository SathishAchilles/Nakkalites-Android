package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.domain.models.Banner
import `in`.nakkalites.mediaclient.domain.models.BannerType
import `in`.nakkalites.mediaclient.viewmodel.BaseModel

class BannerVm(banner: Banner) : BaseModel {
    val name = banner.title
    val type = banner.type
    //    val thumbnail = "https://www.pixelstalk.net/wp-content/uploads/2016/10/Free-bing-daily-wallpaper-url.jpg"
    val thumbnail = when (banner.type) {
        BannerType.VIDEO -> banner.video!!.thumbnailImage
        BannerType.WEB_SERIES -> banner.webSeries!!.thumbnailImage
        else -> null
    }
}