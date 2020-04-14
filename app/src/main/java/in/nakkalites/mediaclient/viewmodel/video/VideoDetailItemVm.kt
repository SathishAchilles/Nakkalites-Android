package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.utils.toTimeString

class VideoDetailItemVm(video: Video) : BaseModel {
    val title = video.videoName
    val url = video.url
    val thumbnail = video.thumbnailImage
    val description = video.description
    val name = video.videoName
    val duration = ((video.duration ?: 0) - (video.lastPlayedTime ?: 0))
        .toTimeString(withLiteral = false, includeZeros = true)
    val progress = if (video.lastPlayedTime != null && video.duration != null) {
        (100 * (video.lastPlayedTime / video.duration.toFloat())).toInt()
    } else {
        0
    }
    val showProgress = progress != 0
    val starring = video.starring

}
