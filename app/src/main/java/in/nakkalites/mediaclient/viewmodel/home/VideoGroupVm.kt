package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.domain.models.VideoGroup
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.databinding.ObservableArrayList

class VideoGroupVm(videoGroup: VideoGroup) : BaseModel {
    val name = videoGroup.name
    val items = ObservableArrayList<VideoVm>()

    init {
        val list = videoGroup.videos.mapIndexed { index, video ->
            VideoVm(index, video)
        }
        items.addAll(list)
    }
}
