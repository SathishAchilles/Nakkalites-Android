package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.view.video.PlayerTracker
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel

class VideoPlayerVm(val videoGroupDomain: VideoGroupDomain) : BaseViewModel(), PlayerTracker {
    var id: String? = null
    var name: String? = null
    var thumbnail: String? = null
    var url: String? = null

    override var duration: Long = 0
    override var shouldPauseCurrentVideo = true

    fun setArgs(id: String, name: String, thumbnail: String, url: String) {
        this.id = id
        this.name = name
        this.thumbnail = thumbnail
        this.url = url
    }
}
