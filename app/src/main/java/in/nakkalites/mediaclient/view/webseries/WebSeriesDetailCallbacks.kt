package `in`.nakkalites.mediaclient.view.webseries

import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesDetailItemVm

interface WebSeriesDetailCallbacks {
    fun onVideoClick(vm: VideoVm?)
    fun onShareClick(vm: WebSeriesDetailItemVm)
}
