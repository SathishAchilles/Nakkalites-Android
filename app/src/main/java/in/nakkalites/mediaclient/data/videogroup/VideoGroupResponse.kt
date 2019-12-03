package `in`.nakkalites.mediaclient.data.videogroup

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoGroupResponse(
    @field:Json(name = "banners") val banners: List<Banner>,
    @field:Json(name = "video_list") val videosGroup: List<VideoGroup>,
    @field:Json(name = "cursor") val cursor: String
)

@JsonClass(generateAdapter = true)
data class Banner(
    @field:Json(name = "title") val title: String,
    @field:Json(name = "web_series") val webSeries: WebSeries?,
    @field:Json(name = "type") val type: String,
    @field:Json(name = "video") val video: Video?
)

@JsonClass(generateAdapter = true)
data class VideoGroup(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "header_name") val name: String,
    @field:Json(name = "videos") val videos: List<Video>
)

@JsonClass(generateAdapter = true)
data class Video(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title_name") val titleName: String,
    @field:Json(name = "video_name") val videoName: String,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "thumbnail_image") val thumbnailImage: String
)

@JsonClass(generateAdapter = true)
data class WebSeries(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title_name") val titleName: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "thumbnail_image") val thumbnailImage: String
)
