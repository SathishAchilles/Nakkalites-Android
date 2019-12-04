package `in`.nakkalites.mediaclient.data.videogroup

import `in`.nakkalites.mediaclient.data.utils.Page
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoGroupResponse(
    @field:Json(name = "banners") val banners: List<BannerEntity>,
    @field:Json(name = "video_list") val videoGroups: List<VideoGroupEntity>
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
    @field:Json(name = "videos") val videos: List<VideoEntity>
)

@JsonClass(generateAdapter = true)
data class VideoEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title_name") val titleName: String,
    @field:Json(name = "video_name") val videoName: String,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "thumbnail_image") val thumbnail: String
)

@JsonClass(generateAdapter = true)
data class WebSeriesEntity(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title_name") val titleName: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "thumbnail_image") val thumbnail: String
)
