package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText

class VideoListHeader : BaseModel {
    val title = DisplayText.Singular(R.string.related_videos)
}
