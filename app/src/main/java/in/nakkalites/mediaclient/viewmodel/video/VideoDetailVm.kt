package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.utils.PagingBody
import `in`.nakkalites.mediaclient.domain.utils.PagingCallback
import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.EmptyStateVm
import `in`.nakkalites.mediaclient.viewmodel.utils.RxTransformers
import `in`.nakkalites.mediaclient.viewmodel.utils.toCamelCase
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class VideoDetailVm(private val videoGroupDomain: VideoGroupDomain) : BaseViewModel() {
    private var pagingBody: PagingBody = PagingBody(pagingCallback = null)
    val items = ObservableArrayList<BaseModel>()
    private val isDataLoading = ObservableBoolean()
    var id: String? = null
    var name: String? = null
    var thumbnail: String? = null
    var url: String? = null
    var duration: Long? = 0L
    var lastPlayedTime: Long? = 0L
    val pageTitle = ObservableField<String>()
    private val viewState = MutableLiveData<Event<Result<Unit>>>()

    fun viewStates(): LiveData<Event<Result<Unit>>> = viewState

    fun setArgs(id: String, name: String, thumbnail: String, url: String) {
        this.id = id
        this.name = name
        this.thumbnail = thumbnail
        this.url = url
        pageTitle.set(name.toCamelCase())
    }

    fun loading(): Boolean {
        return isDataLoading.get()
    }

    internal fun initPagingBody(pagingCallback: PagingCallback) {
        pagingBody = PagingBody(pagingCallback = pagingCallback)
    }

    fun fetchVideoDetail(id: String) {
        disposables += getRequestObservable(id, pagingBody)
            .map { handleEmptyPage(it.toMutableList()) }
            .observeOn(AndroidSchedulers.mainThread())
            .compose(RxTransformers.dataLoading(isDataLoading, items))
            .subscribeBy(
                onSuccess = {
                    items.addAll(it)
                },
                onError = {
                    viewState.value = Event(Result.Error(Unit, throwable = it))
                }
            )
    }

    private fun getRequestObservable(id: String, pagingBody: PagingBody): Single<List<BaseModel>> {
        val relatedVideos = videoGroupDomain.getRelatedVideos(id, pagingBody)
            .doAfterSuccess {
                pagingBody.onNextPage(it.first.size, it.second)
            }
            .onErrorReturn { Pair(listOf(), null) }
            .map {
                listOf<BaseModel>(VideoListHeader()) +
                        it.first.mapIndexed { index, video -> VideoVm(video, index) }
            }
        return if (pagingBody.isFirstPage()) {
            val videoDetail = videoGroupDomain.getVideoDetail(id)
                .map { video ->
                    duration = video.duration
                    lastPlayedTime = video.lastPlayedTime
                    video
                }
                .map { video ->
                    listOf<BaseModel>(VideoDetailItemVm(video))
                }
            Single.zip(videoDetail, relatedVideos, BiFunction { t1, t2 -> t1 + t2 })
        } else {
            relatedVideos
        }
    }

    private fun handleEmptyPage(viewModels: MutableList<BaseModel>): List<BaseModel> {
        if (viewModels.isEmpty()) {
            viewModels.add(EmptyStateVm(R.layout.empty_state))
        }
        return viewModels
    }
}
