package `in`.nakkalites.mediaclient.viewmodel.webseries

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.utils.toTimeString

class SeasonEpisodeItemVm(
    val seasonId: String, val seasonName: String, episodeNumber: Int, episode: Video
) :
    BaseModel {
    val id = episode.id
    val title = episode.videoName
//        DisplayText.Singular(R.string.episode_title, listOf(episodeNumber, episode.videoName))
    val showDuration = episode.duration != null
    val duration = episode.duration?.toTimeString() ?: ""
    val imageUrl = episode.thumbnailImage
    val url = episode.url
    val showProgress = episode.duration != null && episode.lastPlayedTime != null
    val progressPercent = if (episode.lastPlayedTime != null && episode.duration != null) {
        (100 * (episode.lastPlayedTime / episode.duration.toFloat())).toInt()
    } else {
        0
    }
}
