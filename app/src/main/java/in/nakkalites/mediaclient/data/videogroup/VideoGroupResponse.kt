package `in`.nakkalites.mediaclient.data.videogroup

import `in`.nakkalites.mediaclient.data.utils.Page
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoGroupResponse(
    @field:Json(name = "banners") val banners: List<BannerEntity>,
    @field:Json(name = "video_list") val videoGroups: List<VideoGroupEntity> = listOf()
) : Page() {
    override val pageSize = videoGroups.size
}

@JsonClass(generateAdapter = true)
data class BannerEntity(
    @field:Json(name = "title") val title: String,
    @field:Json(name = "web_series") val webSeries: WebSeriesEntity?,
    @field:Json(name = "type") val type: String,
    @field:Json(name = "video") val video: VideoEntity?
)

@JsonClass(generateAdapter = true)
data class VideoGroupEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "header_name") val name: String,
    @field:Json(name = "videos") val videos: List<VideoEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class VideoEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title_name") val titleName: String,
    @field:Json(name = "video_name") val videoName: String,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "last_played_time") val lastPlayedTime: Long?,
    @field:Json(name = "duration") val duration: Long?,
    @field:Json(name = "thumbnail_image") val thumbnail: String,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "starring") val starring: String?,
    @field:Json(name = "share_text") val shareText: String?,
    @field:Json(name = "video_list") val videoGroups: List<VideoGroupEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class WebSeriesEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "no_of_seasons") val seasonsCount: Int,
    @field:Json(name = "thumbnail_image") val thumbnail: String,
    @field:Json(name = "next_webseries_number") val nextWebseriesNumber: Int?,
    @field:Json(name = "next_episode_number") val nextEpisodeNumber: Int?,
    @field:Json(name = "next_video_id") val nextVideoId: String?,
    @field:Json(name = "description") val description: String,
    @field:Json(name = "starring") val starring: String?,
    @field:Json(name = "seasons") val seasons: List<SeasonEntity> = listOf()
)

@JsonClass(generateAdapter = true)
data class WebSeriesDetailResponse(
    @field:Json(name = "web_series") val webSeries: WebSeriesEntity
)

@JsonClass(generateAdapter = true)
data class SeasonEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "episodes") val episodes: List<VideoEntity> = listOf()
)
