package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.subscription.PlanUtils
import `in`.nakkalites.mediaclient.viewmodel.utils.toTimeString
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt

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
    val shouldPlay = video.isPlayable
    val showAds = video.showAds
    val hasPlan = video.plan != null
    val planName = video.plan?.name
    val planImg = ObservableField(PlanUtils.getPlanIcon(video.plan))
    val planColorInt = ObservableInt(PlanUtils.getPlanColorInt(video.plan?.colorCode))
    val descriptionExpanded = ObservableBoolean(false)
    val descriptionShowReadMore = ObservableBoolean(false)
    val starringExpanded = ObservableBoolean(false)
    val starringShowReadMore = ObservableBoolean(false)
    val maxLines = Int.MAX_VALUE
    val minLines = 4

    fun showStarringReadMore(expand: Boolean) {
        starringShowReadMore.set(!expand)
    }

    fun toggleStarringExpandClick() {
        starringExpanded.set(!starringExpanded.get())
    }
    fun showDescriptionReadMore(expand: Boolean) {
        descriptionShowReadMore.set(!expand)
    }

    fun toggleDescriptionExpandClick() {
        descriptionExpanded.set(!descriptionExpanded.get())
    }
}
