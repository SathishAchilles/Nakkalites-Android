package `in`.nakkalites.mediaclient.viewmodel.webseries

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.models.WebSeries
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm

class WebSeriesDetailItemVm(webSeries: WebSeries) : BaseModel {
    val episodesCount = DisplayText.Singular(R.string.x_seasons, listOf(webSeries.episodesCount))
    val nextEpisodeButtonText =
        DisplayText.Singular(R.string.play_episode_x, listOf(webSeries.nextEpisodeNumber))
    val description = webSeries.description
    val videoVm = VideoVm(webSeries.videoGroups[0].videos[0])
}
