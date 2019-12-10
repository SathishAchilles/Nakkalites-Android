package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel

class VideoDetailItemVm(video: Video) : BaseModel {
    val url = video.url
    val thumbnail = video.thumbnailImage
    val description = video.description
    val shareText = video.shareText
    val name = video.videoName
}
