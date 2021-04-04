package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.subscription.PlanUtils.getPlanColorInt
import `in`.nakkalites.mediaclient.viewmodel.subscription.PlanUtils.getPlanIcon
import androidx.annotation.DimenRes
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt

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
    val adTimes = video.adTimes
    val shouldPlay = video.isPlayable
    val showAds = video.showAds
    val hasPlan = video.plan != null
    val planName = video.plan?.name
    val planImg = ObservableField(getPlanIcon(video.plan))
    val planColorInt = ObservableInt(getPlanColorInt(video.plan?.colorCode))

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

}
