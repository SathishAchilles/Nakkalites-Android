package `in`.nakkalites.mediaclient.domain.models

import `in`.nakkalites.mediaclient.data.user.UserEntity
import `in`.nakkalites.mediaclient.data.videogroup.BannerEntity
import `in`.nakkalites.mediaclient.data.videogroup.VideoEntity
import `in`.nakkalites.mediaclient.data.videogroup.VideoGroupEntity
import `in`.nakkalites.mediaclient.data.videogroup.WebSeriesEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(val id: String, val name: String, val email: String, val imageUrl: String? = null) {
    companion object {
        fun map(userEntity: UserEntity): User = User(
            userEntity.id, userEntity.name, userEntity.email
        )
    }
}

data class Video(
    val id: String, val titleName: String, val videoName: String, val url: String,
    val thumbnailImage: String, val description: String?, val shareText: String?,
    val videoGroups: List<VideoGroup>
) {
    companion object {
        fun map(videoEntity: VideoEntity): Video = Video(
            videoEntity.id, videoEntity.titleName, videoEntity.videoName,
            videoEntity.url, videoEntity.thumbnail, videoEntity.description, videoEntity.shareText,
            videoEntity.videoGroups.map { VideoGroup.map(it) }
        )
    }
}

data class WebSeries(
    val id: String, val titleName: String, val name: String, val thumbnailImage: String,
    val episodesCount: Int, val description: String, val videoGroups: List<VideoGroup>
) {
    companion object {
        fun map(webSeriesEntity: WebSeriesEntity): WebSeries = WebSeries(
            webSeriesEntity.id, webSeriesEntity.titleName, webSeriesEntity.name,
            webSeriesEntity.thumbnail, webSeriesEntity.episodesCount, webSeriesEntity.description,
            webSeriesEntity.videoGroups.map { VideoGroup.map(it) }
        )
    }
}

data class Banner(
    val title: String, val webSeries: WebSeries?, val type: BannerType?, val video: Video?
) {
    companion object {
        fun map(bannerEntity: BannerEntity): Banner = Banner(
            bannerEntity.title,
            bannerEntity.webSeries?.let { WebSeries.map(bannerEntity.webSeries) },
            getBannerType(bannerEntity.type),
            bannerEntity.video?.let { Video.map(bannerEntity.video) }
        )
    }
}

data class VideoGroup(
    val id: String, val name: String, val videos: List<Video>
) {
    companion object {
        fun map(videoGroupEntity: VideoGroupEntity): VideoGroup = VideoGroup(
            videoGroupEntity.id, videoGroupEntity.name,
            videoGroupEntity.videos.map { Video.map(it) }
        )
    }
}

fun getBannerType(type: String) = when (type) {
    "video" -> BannerType.VIDEO
    "webseries" -> BannerType.WEB_SERIES
    else -> null
}
