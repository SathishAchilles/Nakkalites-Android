package `in`.nakkalites.mediaclient.data.videogroup

import com.squareup.moshi.Json

data class VideoGroupResponse(
    @field:Json(name = "banners") val banners: List<Banner>,
    @field:Json(name = "video_list") val videosList: List<Video>,
    @field:Json(name = "cursor") val cursor: String
)

data class Banner(
    @field:Json(name = "title") val title: String,
    @field:Json(name = "web_series") val webSeries: WebSeries?,
    @field:Json(name = "type") val type: String,
    @field:Json(name = "video") val video: Video?
)

data class Video(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title_name") val titleName: String,
    @field:Json(name = "video_name") val videoName: String,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "thumbnail_image") val thumbnailImage: String
)

data class WebSeries(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title_name") val titleName: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "thumbnail_image") val thumbnailImage: String
)
