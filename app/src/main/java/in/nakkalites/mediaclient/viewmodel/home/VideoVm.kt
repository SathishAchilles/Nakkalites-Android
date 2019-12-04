package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel

class VideoVm(video: Video) : BaseModel {
    val id = video.id
    val name = video.videoName
    val url = video.url
    val thumbnail = video.thumbnailImage
}
