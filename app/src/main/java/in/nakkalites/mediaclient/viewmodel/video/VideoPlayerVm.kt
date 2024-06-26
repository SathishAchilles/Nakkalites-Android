package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.video.PlayerTracker
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

class VideoPlayerVm(
    private val videoGroupDomain: VideoGroupDomain
) : BaseViewModel(), PlayerTracker {
    var id: String? = null
    var name: String? = null
    var thumbnail: String? = null
    var url: String? = null
    var disposable: Disposable? = null
    private val viewState = MutableLiveData<Event<Result<Pair<Long, Long>>>>()

    fun viewStates(): LiveData<Event<Result<Pair<Long, Long>>>> = viewState

    fun setArgs(id: String, name: String, thumbnail: String, url: String) {
        this.id = id
        this.name = name
        this.thumbnail = thumbnail
        this.url = url
    }

    override var duration: Long = 0

    override var timeElapsed: Long = 0

    override var shouldPauseCurrentVideo = false

    fun uploadVideoProgress() {
        disposable?.dispose()
        disposable = videoGroupDomain.trackVideo(id!!, duration, timeElapsed)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    viewState.value = Event(Result.Success(duration to timeElapsed))
                },
                onError = {
                    viewState.value = Event(Result.Error(throwable = it))
                }
            )
    }

}
