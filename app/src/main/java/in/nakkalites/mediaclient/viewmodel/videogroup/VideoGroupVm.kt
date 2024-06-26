package `in`.nakkalites.mediaclient.viewmodel.videogroup

import `in`.nakkalites.mediaclient.domain.models.VideoGroup
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import androidx.databinding.ObservableArrayList

class VideoGroupVm(videoGroup: VideoGroup) : BaseViewModel() {
    val id = videoGroup.id
    val name = videoGroup.name
    val category = videoGroup.type
    val items = ObservableArrayList<VideoVm>()

    init {
        val list = videoGroup.videos.mapIndexed { index, video ->
            VideoVm(video, index, addMarginStart = true)
        }
        items.addAll(list)
    }
}
