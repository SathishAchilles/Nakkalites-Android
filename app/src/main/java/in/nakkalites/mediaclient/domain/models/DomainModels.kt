package `in`.nakkalites.mediaclient.domain.models

import `in`.nakkalites.mediaclient.data.user.UserEntity
import `in`.nakkalites.mediaclient.data.videogroup.*
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String, val name: String?, val email: String?, val imageUrl: String? = null
) {
    companion object {
        fun map(userEntity: UserEntity): User =
            User(userEntity.id, userEntity.name, userEntity.email, userEntity.imageUrl)
    }
}

data class Video(
    val id: String, val titleName: String, val videoName: String, val url: String,
    val thumbnailImage: String, val description: String?, val duration: Long?,
    val lastPlayedTime: Long?, val starring: String?, val videos: List<Video>
) {
    companion object {
        fun map(videoEntity: VideoEntity): Video = Video(
            videoEntity.id, videoEntity.titleName, videoEntity.videoName,
            videoEntity.url, videoEntity.thumbnail, videoEntity.description,
            videoEntity.duration, videoEntity.lastPlayedTime, videoEntity.starring,
            videoEntity.videos.map { map(it) }
        )
    }
}

data class WebSeries(
    val id: String, val name: String, val thumbnailImage: String, val webSeriesCount: Int,
    val nextWebSeriesNumber: Int?, val nextEpisodeNumber: Int, val description: String,
    val nextVideoId: String?, val starring: String?, val seasons: List<Season>
) {
    companion object {
        fun map(webSeriesEntity: WebSeriesEntity): WebSeries = WebSeries(
            webSeriesEntity.id, webSeriesEntity.name,
            webSeriesEntity.thumbnail, webSeriesEntity.seasonsCount,
            webSeriesEntity.nextWebseriesNumber, webSeriesEntity.nextEpisodeNumber ?: 1,
            webSeriesEntity.description, webSeriesEntity.nextVideoId, webSeriesEntity.starring,
            webSeriesEntity.seasons.map { Season.map(it) }
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

data class Season(
    val id: String, val name: String, val episodes: List<Video>
) {
    companion object {
        fun map(seasonEntity: SeasonEntity): Season = Season(
            seasonEntity.id, seasonEntity.name,
            seasonEntity.episodes.map { Video.map(it) }
        )
    }
}

fun getBannerType(type: String) = when (type) {
    "video" -> BannerType.VIDEO
    "webseries" -> BannerType.WEB_SERIES
    else -> null
}
