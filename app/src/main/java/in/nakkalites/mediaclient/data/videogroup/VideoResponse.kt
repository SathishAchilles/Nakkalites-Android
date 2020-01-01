package `in`.nakkalites.mediaclient.data.videogroup

import `in`.nakkalites.mediaclient.data.utils.Page
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class VideosResponse(
    @field:Json(name = "video_group") val videoGroup: VideoGroupEntity
) : Page() {
    override val pageSize = videoGroup.videos.size
}

@JsonClass(generateAdapter = true)
data class VideoDetailResponse(
    @field:Json(name = "video_info") val video: VideoEntity
)
