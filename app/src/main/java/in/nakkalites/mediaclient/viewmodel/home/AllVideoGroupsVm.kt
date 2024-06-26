package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.logging.logd
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.subscription.PlanManager
import `in`.nakkalites.mediaclient.domain.utils.PagingBody
import `in`.nakkalites.mediaclient.domain.utils.PagingCallback
import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.EmptyStateVm
import `in`.nakkalites.mediaclient.viewmodel.utils.RxTransformers
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupVm
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class AllVideoGroupsVm(private val videoGroupDomain: VideoGroupDomain, planManager: PlanManager) :
        BaseViewModel() {
    private var pagingBody: PagingBody = PagingBody(pagingCallback = null)
    val isRefreshing = ObservableBoolean()
    val items = ObservableArrayList<BaseModel>()
    private val isLoading = ObservableBoolean()
    private val viewState = MutableLiveData<Event<Result<Unit?>>>()
    val showErrorPage = ObservableBoolean()

    init {
        disposables += planManager.getPlanObserver()
                .filter { it }
                .observeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribeBy(onNext = {
                    pagingBody.reset()
                    fetchVideoGroups()
                    logd("Refresh Page")
                })
    }

    fun viewStates(): LiveData<Event<Result<Unit?>>> = viewState

    internal fun initPagingBody(pagingCallback: PagingCallback) {
        pagingBody = PagingBody(pagingCallback = pagingCallback)
    }

    internal fun fetchVideoGroups() {
        disposables += videoGroupDomain.getAllVideoGroups(pagingBody)
                .doOnSubscribe { showErrorPage.set(false) }
                .doAfterSuccess {
                    pagingBody.onNextPage(it.second.size, it.third)
                }
                .map {
                    (if (it.first.isNotEmpty()) listOf(BannersVm(it.first)) else listOf()) +
                            it.second.map { videoGroup -> VideoGroupVm(videoGroup) }
                }
                .map { handleEmptyPage(it.toMutableList()) }
                .observeOn(mainThread())
                .compose(RxTransformers.dataLoading(isLoading, items))
                .doFinally { isRefreshing.set(false) }
                .subscribeBy(
                        onSuccess = {
                            items.addAll(it)
                        },
                        onError = {
                            viewState.value = Event(Result.Error(Unit, throwable = it))
                            if (pagingBody.isFirstPage()) {
                                showErrorPage.set(true)
                            }
                        }
                )
    }

    fun loading() = isLoading.get()

    fun refreshList() {
        showErrorPage.set(false)
        isRefreshing.set(true)
        pagingBody.reset()
        disposables.clear()
        items.clear()
        fetchVideoGroups()
    }

    private fun handleEmptyPage(viewModels: MutableList<BaseModel>): List<BaseModel> {
        if (pagingBody.isFirstPage() && viewModels.isEmpty()) {
            viewModels.add(EmptyStateVm(R.layout.empty_state))
        }
        return viewModels
    }
}
