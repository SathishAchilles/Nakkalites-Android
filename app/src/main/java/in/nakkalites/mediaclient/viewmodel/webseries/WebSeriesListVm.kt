package `in`.nakkalites.mediaclient.viewmodel.webseries

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
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class WebSeriesListVm(private val videoGroupDomain: VideoGroupDomain, planManager: PlanManager) :
    BaseViewModel() {
    internal val isRefreshing = ObservableBoolean()
    val items = ObservableArrayList<BaseModel>()
    private val isLoading = ObservableBoolean()
    private var pagingBody: PagingBody = PagingBody(pagingCallback = null)
    private val viewState = MutableLiveData<Event<Result<Unit>>>()
    val showErrorPage = ObservableBoolean()

    init {
        disposables += planManager.getPlanObserver()
            .filter { it }
            .observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                pagingBody.reset()
                fetchWebSeriesList()
                logd("Refresh Page")
            })
    }

    fun viewStates(): LiveData<Event<Result<Unit>>> = viewState

    internal fun initPagingBody(pagingCallback: PagingCallback) {
        pagingBody = PagingBody(pagingCallback = pagingCallback)
    }

    internal fun fetchWebSeriesList() {
        disposables += videoGroupDomain.getWebSeriesList(pagingBody)
                .doOnSubscribe { showErrorPage.set(false) }
                .doAfterSuccess {
                pagingBody.onNextPage(it.first.size, it.second)
            }
            .map {
                it.first.map { webSeries -> WebSeriesVm(webSeries) }
            }
            .map { handleEmptyPage(it.toMutableList()) }
            .observeOn(AndroidSchedulers.mainThread())
            .compose(RxTransformers.dataLoading(isLoading, items))
            .doFinally { isRefreshing.set(false) }
            .subscribeBy(
                onSuccess = {
                    items.addAll(it)
                    viewState.value = Event(Result.Success(Unit))
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
        fetchWebSeriesList()
    }

    private fun handleEmptyPage(viewModels: MutableList<BaseModel>): List<BaseModel> {
        if (pagingBody.isFirstPage() && viewModels.isEmpty()) {
            viewModels.add(EmptyStateVm(R.layout.empty_state))
        }
        return viewModels
    }
}
