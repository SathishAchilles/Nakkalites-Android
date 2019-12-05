package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.utils.PagingBody
import `in`.nakkalites.mediaclient.domain.utils.PagingCallback
import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.EmptyStateVm
import `in`.nakkalites.mediaclient.viewmodel.utils.RxTransformers
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class WebSeriesListVm(val videoGroupDomain: VideoGroupDomain) : BaseViewModel() {
    internal val isRefreshing = ObservableBoolean()
    internal val isLoading = ObservableBoolean()
    private lateinit var pagingBody: PagingBody
    val items = ObservableArrayList<BaseModel>()

    internal fun initPagingBody(pagingCallback: PagingCallback) {
        pagingBody = PagingBody(pagingCallback = pagingCallback)
    }

    internal fun fetchWebSeriesList() {
        disposables += videoGroupDomain.getWebSeriesList(pagingBody)
            .doAfterSuccess {
                pagingBody.onNextPage(it.first.size, it.second)
            }
            .map {
                it.first.map { webSeries -> WebSeriesVm(webSeries) }
            }
            .map { handleEmptyPage(it.toMutableList()) }
            .observeOn(AndroidSchedulers.mainThread())
            .compose(RxTransformers.dataLoading(isLoading, items))
            .doFinally {
                isRefreshing.set(false)
            }
            .subscribeBy(
                onSuccess = {
                    items.addAll(it)
                    loge("items $it")
                },
                onError = {
                    loge("Error $it")
                    it.printStackTrace()
                }
            )
    }

    fun loading() = isLoading.get()

    fun refreshList() {
        items.clear()
        pagingBody.reset()
        fetchWebSeriesList()
    }

    private fun handleEmptyPage(viewModels: MutableList<BaseModel>): List<BaseModel> {
        if (pagingBody.isFirstPage() && viewModels.isEmpty()) {
            viewModels.add(EmptyStateVm(R.layout.empty_state_home))
        }
        return viewModels
    }
}
