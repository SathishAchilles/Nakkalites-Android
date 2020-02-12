package `in`.nakkalites.mediaclient.data.videogroup

import `in`.nakkalites.mediaclient.data.utils.Page
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class VideoDetailResponse(
    @field:Json(name = "video") val video: VideoEntity
)

@JsonClass(generateAdapter = true)
data class VideosResponse(
    @field:Json(name = "videos") val videos: List<VideoEntity>
) : Page() {
    override val pageSize = videos.size
}
