package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.annotation.DimenRes

class VideoVm(
    video: Video, position: Int = -1, addMarginStart: Boolean = false,
    val showVideoTitle: Boolean = false
) : BaseModel {
    val id = video.id
    val name = video.videoName
    val url = video.url
    val thumbnail = video.thumbnailImage
    val description = video.description
    val titleVisibility = showVideoTitle
    val showProgress = video.lastPlayedTime != null
    val progressPercent: Int = if (video.lastPlayedTime != null && video.duration != null) {
        (100 * (video.lastPlayedTime / video.duration.toFloat())).toInt()
    } else {
        0
    }
    val duration = video.duration
    val lastPlayedTime = video.lastPlayedTime

    @DimenRes
    val marginStart =
        if (position == 0 && addMarginStart) {
            R.dimen.horizontal_list_margin_start_initial
        } else {
            R.dimen.horizontal_list_margin_start_default
        }
}
