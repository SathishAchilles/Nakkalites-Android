package `in`.nakkalites.mediaclient.domain.models

import `in`.nakkalites.mediaclient.data.subscription.PlanEntity
import `in`.nakkalites.mediaclient.data.user.LoginUserEntity
import `in`.nakkalites.mediaclient.data.user.UserResponse
import `in`.nakkalites.mediaclient.data.videogroup.*
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String, val name: String?, val email: String?, val imageUrl: String? = null,
    val providerType: String? = null, val countryCode: String?, val phoneNumber: String?,
    val gender: String?, val dob: String?, val city: String?, val country: String?,
    val plan: Plan? = null, val upgradablePlan: Plan? = null, val isFirstLogin: Boolean = false
) {

    companion object {
        fun map(userEntity: LoginUserEntity): User =
            User(
                userEntity.id, userEntity.name, userEntity.email, userEntity.imageUrl,
                userEntity.providerType, userEntity.countryCode, userEntity.phoneNumber,
                userEntity.gender, userEntity.dob, userEntity.city, userEntity.country,
                isFirstLogin = userEntity.isFirstLogin
            )

        fun map(userEntity: UserResponse): User =
            User(
                userEntity.user.id, userEntity.user.name, userEntity.user.email,
                userEntity.user.imageUrl, userEntity.user.providerType, userEntity.user.countryCode,
                userEntity.user.phoneNumber, userEntity.user.gender, userEntity.user.dob,
                userEntity.user.city, userEntity.user.country,
                plan = userEntity.activePlan?.let { Plan.map(it) },
                upgradablePlan = userEntity.upgradablePlan?.let { Plan.map(it) },
            )
    }
}

@JsonClass(generateAdapter = true)
data class Plan(
    val id: String?, val name: String?, val price: String?, val frequency: String?,
    val contentTags: List<String>?, val planType: String?, val colorCode: String?,
    val description: String?, val availablePlansCount: Int?, val promotionText: String?
) {
    companion object {
        fun map(planEntity: PlanEntity): Plan =
            Plan(
                planEntity.id, planEntity.name, planEntity.price, planEntity.frequency,
                planEntity.contentTags, planEntity.planType, planEntity.colorCode,
                planEntity.description, planEntity.availablePlansCount, planEntity.promotionText
            )
    }
}

data class Video(
    val id: String, val titleName: String, val videoName: String, val url: String?,
    val thumbnailImage: String, val description: String?, val duration: Long?,
    val lastPlayedTime: Long?, val starring: String?, val adTimes: List<Long>,
    val plan: Plan?, val isPlayable: Boolean?, val showAds: Boolean?,
) {
    companion object {
        fun map(videoEntity: VideoEntity): Video = Video(
            videoEntity.id, videoEntity.titleName, videoEntity.videoName,
            videoEntity.url, videoEntity.thumbnail, videoEntity.description, videoEntity.duration,
            videoEntity.lastPlayedTime, videoEntity.starring, videoEntity.adTimes,
            videoEntity.plan?.let { Plan.map(it) }, videoEntity.isPlayable, videoEntity.showAds
        )
    }
}

data class WebSeries(
    val id: String, val name: String, val thumbnailImage: String, val webSeriesCount: Int,
    val nextSeasonNumber: Int, val nextEpisodeNumber: Int, val description: String?,
    val nextVideo: Video?, val starring: String?, val seasons: List<Season>,
    val plan: Plan?, val isPlayable: Boolean?, val showAds: Boolean?,
) {
    companion object {
        fun map(webSeriesEntity: WebSeriesEntity): WebSeries = WebSeries(
            webSeriesEntity.id, webSeriesEntity.name,
            webSeriesEntity.thumbnail, webSeriesEntity.seasonsCount ?: 1,
            webSeriesEntity.nextSeasonNumber ?: 1, webSeriesEntity.nextEpisodeNumber ?: 1,
            webSeriesEntity.description, webSeriesEntity.nextVideo?.let { Video.map(it) },
            webSeriesEntity.starring, webSeriesEntity.seasons.map { Season.map(it) },
            webSeriesEntity.plan?.let { Plan.map(it) }, webSeriesEntity.isPlayable,
            webSeriesEntity.showAds
        )
    }
}

data class Banner(
    val id: String, val title: String, val webSeries: WebSeries?, val type: BannerType?,
    val video: Video?
) {
    companion object {
        fun map(bannerEntity: BannerEntity): Banner = Banner(
            bannerEntity.id, bannerEntity.title,
            bannerEntity.webSeries?.let { WebSeries.map(bannerEntity.webSeries) },
            getBannerType(bannerEntity.type),
            bannerEntity.video?.let { Video.map(bannerEntity.video) }
        )
    }
}

data class VideoGroup(
    val id: String, val name: String, val type: String, val videos: List<Video>
) {
    companion object {
        fun map(videoGroupEntity: VideoGroupEntity): VideoGroup = VideoGroup(
            videoGroupEntity.id, videoGroupEntity.name, videoGroupEntity.type,
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
    "Single" -> BannerType.VIDEO
    "WebSeries" -> BannerType.WEB_SERIES
    else -> null
}
