package `in`.nakkalites.mediaclient.viewmodel.webseries

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.domain.models.WebSeries
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.subscription.PlanUtils
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.utils.formatEn
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt

class WebSeriesDetailItemVm(webSeries: WebSeries) : BaseModel {
    val name = webSeries.name
    val webseriesCount = DisplayText.Plural(
        R.plurals.x_season, webSeries.webSeriesCount, listOf(webSeries.webSeriesCount)
    )
    private val nextEpisodeText = DisplayText.Singular(
        R.string.s_x_e_y,
        listOf(
            "%02d".formatEn(webSeries.nextSeasonNumber),
            "%02d".formatEn(webSeries.nextEpisodeNumber)
        )
    )
    val nextEpisodeButtonText =
        DisplayText.Singular(R.string.play_episode_x, listOf(nextEpisodeText))
    val description = webSeries.description
    val starring = webSeries.starring
    private val nextVideo: Video? =
        webSeries.nextVideo ?: webSeries.seasons.firstOrNull()?.episodes?.firstOrNull()
    val videoVm: VideoVm? = nextVideo?.let { VideoVm(nextVideo) }
    val planName = webSeries.plan?.name
    val planImg = ObservableField(PlanUtils.getPlanIcon(webSeries.plan))
    val planColorInt = ObservableInt(PlanUtils.getPlanColorInt(webSeries.plan?.colorCode))
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
