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
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupVm
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class AllVideoGroupsVm(private val videoGroupDomain: VideoGroupDomain) : BaseViewModel() {
    private lateinit var pagingBody: PagingBody
    val isRefreshing = ObservableBoolean()
    val items = ObservableArrayList<BaseModel>()
    private val isLoading = ObservableBoolean()

    internal fun initPagingBody(pagingCallback: PagingCallback) {
        pagingBody = PagingBody(pagingCallback = pagingCallback)
    }

    internal fun fetchVideoGroups() {
        disposables += videoGroupDomain.getAllVideoGroups(pagingBody)
            .doAfterSuccess {
                pagingBody.onNextPage(it.second.size, it.third)
            }
            .map {
                listOf(BannersVm(it.first)) +
                        it.second.map { videoGroup -> VideoGroupVm(videoGroup) }
            }
            .map { handleEmptyPage(it.toMutableList()) }
            .observeOn(mainThread())
            .compose(RxTransformers.dataLoading(isLoading, items))
            .doFinally { isRefreshing.set(false) }
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
        isRefreshing.set(true)
        items.clear()
        pagingBody.reset()
        fetchVideoGroups()
    }

    private fun handleEmptyPage(viewModels: MutableList<BaseModel>): List<BaseModel> {
        if (pagingBody.isFirstPage() && viewModels.isEmpty()) {
            viewModels.add(EmptyStateVm(R.layout.empty_state_home))
        }
        return viewModels
    }
}