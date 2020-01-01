package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.view.video.PlayerTracker
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import android.annotation.SuppressLint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class VideoPlayerVm(
    private val videoGroupDomain: VideoGroupDomain
) : BaseViewModel(), PlayerTracker {
    var id: String? = null
    var name: String? = null
    var thumbnail: String? = null
    var url: String? = null
    var disposable: Disposable? = null

    fun setArgs(id: String, name: String, thumbnail: String, url: String) {
        this.id = id
        this.name = name
        this.thumbnail = thumbnail
        this.url = url
    }

    override var duration: Long = 0

    override var shouldPauseCurrentVideo = true

    override fun trackVideoProgress(timeElapsed: Long) {
        disposable?.dispose()
        disposable = videoGroupDomain.trackVideo(id!!, duration)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    loge("onComplete")
                },
                onError = Timber::e
            )
    }

}
