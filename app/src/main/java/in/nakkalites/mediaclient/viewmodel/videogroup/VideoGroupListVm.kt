package `in`.nakkalites.mediaclient.viewmodel.videogroup

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.domain.utils.PagingBody
import `in`.nakkalites.mediaclient.domain.utils.PagingCallback
import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.RxTransformers
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class VideoGroupListVm(private val videoGroupDomain: VideoGroupDomain) : BaseViewModel() {
    private var pagingBody: PagingBody = PagingBody(pagingCallback = null)
    val items = ObservableArrayList<BaseModel>()
    private val isLoading = ObservableBoolean()
    private var videoGroupId: String? = null
    private var videoGroupName: String? = null
    val pageTitle = ObservableField<String>()
    private val viewState = MutableLiveData<Event<Result<Unit>>>()

    fun viewStates(): LiveData<Event<Result<Unit>>> = viewState

    internal fun setArgs(videoGroupId: String, videoGroupName: String) {
        this.videoGroupId = videoGroupId
        this.videoGroupName = videoGroupName
        pageTitle.set(videoGroupName)
    }

    internal fun initPagingBody(pagingCallback: PagingCallback) {
        pagingBody = PagingBody(pagingCallback = pagingCallback)
    }

    internal fun fetchVideoGroups(videoGroupId: String) {
        disposables += videoGroupDomain.getVideos(videoGroupId, pagingBody)
            .doAfterSuccess {
                pagingBody.onNextPage(it.first.videos.size, it.second)
            }
            .map {
                Pair(it.first.name, it.first.videos
                    .map { video -> VideoVm(video, showVideoTitle = true) })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .compose(RxTransformers.dataLoading(isLoading, items))
            .subscribeBy(
                onSuccess = {
                    pageTitle.set(it.first)
                    items.addAll(it.second)
                    loge("items ${it.second}")
                },
                onError = {
                    viewState.value = Event(Result.Error(Unit, throwable = it))
                }
            )
    }

    fun loading() = isLoading.get()
}
