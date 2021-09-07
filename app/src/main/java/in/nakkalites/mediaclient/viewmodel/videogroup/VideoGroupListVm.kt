package `in`.nakkalites.mediaclient.viewmodel.videogroup

import `in`.nakkalites.logging.logd
import `in`.nakkalites.mediaclient.domain.subscription.PlanManager
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
import io.reactivex.schedulers.Schedulers

class VideoGroupListVm(private val videoGroupDomain: VideoGroupDomain, planManager: PlanManager) :
    BaseViewModel() {
    private var pagingBody: PagingBody = PagingBody(pagingCallback = null)
    val items = ObservableArrayList<BaseModel>()
    private val isLoading = ObservableBoolean()
    private var videoGroupId: String? = null
    private var videoGroupName: String? = null
    private var videoGroupCategory: String? = null
    val pageTitle = ObservableField<String>()
    private val viewState = MutableLiveData<Event<Result<Unit>>>()

    init {
        disposables += planManager.getPlanObserver()
            .filter { it }
            .observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                pagingBody.reset()
                fetchVideoGroups(videoGroupId!!, videoGroupCategory)
                logd("Refresh Page")
            })
    }

    fun viewStates(): LiveData<Event<Result<Unit>>> = viewState

    internal fun setArgs(videoGroupId: String, videoGroupName: String, videoGroupCategory: String) {
        this.videoGroupId = videoGroupId
        this.videoGroupName = videoGroupName
        this.videoGroupCategory = videoGroupCategory
        pageTitle.set(videoGroupName)
    }

    internal fun initPagingBody(pagingCallback: PagingCallback) {
        pagingBody = PagingBody(pagingCallback = pagingCallback)
    }

    internal fun fetchVideoGroups(videoGroupId: String, category: String?) {
        disposables += videoGroupDomain.getVideos(videoGroupId, category, pagingBody)
            .doAfterSuccess {
                pagingBody.onNextPage(it.first.size, it.second)
            }
            .map {
                it.first.map { video -> VideoVm(video) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .compose(RxTransformers.dataLoading(isLoading, items))
            .subscribeBy(
                onSuccess = {
                    items.addAll(it)
                },
                onError = {
                    viewState.value = Event(Result.Error(Unit, throwable = it))
                }
            )
    }

    fun loading() = isLoading.get()
}
