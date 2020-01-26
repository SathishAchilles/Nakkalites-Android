package `in`.nakkalites.mediaclient.viewmodel.webseries

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.models.WebSeries
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm

class WebSeriesDetailItemVm(webSeries: WebSeries) : BaseModel {
    val name = webSeries.name
    val webseriesCount = DisplayText.Plural(
        R.plurals.x_season, webSeries.webSeriesCount, listOf(webSeries.webSeriesCount)
    )
    val nextEpisodeButtonText =
        DisplayText.Singular(R.string.play_episode_x, listOf(webSeries.nextEpisodeNumber))
    val description = webSeries.description
    val starring = webSeries.starring
    private val nextVideo =
        webSeries.seasons.map { it.episodes.filter { video -> video.id == webSeries.nextVideoId } }
            .flatten().firstOrNull()
    val videoVm: VideoVm = VideoVm(nextVideo ?: webSeries.seasons[0].episodes[0])
}
