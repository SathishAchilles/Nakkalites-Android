package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.annotation.DimenRes

class VideoVm(
    video: Video, position: Int = -1, addMarginStart: Boolean = false,
    showVideoTitle: Boolean = false
) : BaseModel {
    val id = video.id
    val name = video.videoName
    val url = video.url
    val thumbnail = video.thumbnailImage
    val description = video.description
    val titleVisibility = showVideoTitle
    val progressPercent: Int = if (video.lastPlayedTime != null && video.duration != null) {
        (100 * (video.lastPlayedTime / video.duration.toFloat())).toInt()
    } else {
        0
    }
    val showProgress = progressPercent != 0
    val duration = video.duration
    val lastPlayedTime = video.lastPlayedTime

    @DimenRes
    val marginStart =
        if (position == 0 && addMarginStart) {
            R.dimen.horizontal_list_margin_start_initial
        } else {
            R.dimen.horizontal_list_margin_start_default
        }

    @DimenRes
    val marginGridStart =
        if (position % 2 == 0) {
            R.dimen.horizontal_list_margin_start_initial
        } else {
            R.dimen.video_default_margin
        }
    @DimenRes
    val marginGridEnd =
        if (position % 2 == 0) {
            R.dimen.video_default_margin
        } else {
            R.dimen.horizontal_list_margin_start_initial
        }

    init {
        loge(" ${video.videoName} ${video.lastPlayedTime} ${video.duration} $progressPercent")
    }
}
