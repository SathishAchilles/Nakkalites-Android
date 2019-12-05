package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.annotation.DimenRes

class VideoVm(position: Int, video: Video) : BaseModel {
    val id = video.id
    val name = video.videoName
    val url = video.url
    val thumbnail = video.thumbnailImage
    @DimenRes
    val marginStart =
        if (position == 0) {
            R.dimen.horizontal_list_margin_start_initial
        } else {
            R.dimen.horizontal_list_margin_start_default
        }
}
