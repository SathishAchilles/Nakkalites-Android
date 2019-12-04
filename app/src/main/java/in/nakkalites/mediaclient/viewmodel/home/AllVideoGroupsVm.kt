package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.utils.PagingBody
import `in`.nakkalites.mediaclient.domain.utils.PagingCallback
import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.view.binding.DummyVm
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.RxTransformers
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class AllVideoGroupsVm(private val videoGroupDomain: VideoGroupDomain) : BaseViewModel() {
    internal val isRefreshing = ObservableBoolean()
    internal val isLoading = ObservableBoolean()
    val items = ObservableArrayList<BaseModel>()
    private lateinit var pagingBody: PagingBody

    internal fun initPagingBody(pagingCallback: PagingCallback) {
        pagingBody = PagingBody(pagingCallback = pagingCallback)
    }

    internal fun fetchVideoGroups() {
        disposables += videoGroupDomain.getAllVideoGroups(pagingBody)
            .doAfterSuccess {
                pagingBody.onNextPage(it.second.size, it.third)
            }
            .observeOn(mainThread())
            .compose(RxTransformers.dataLoading(isLoading, items))
            .subscribeBy(
                onSuccess = {
                    val list = listOf(BannersVm(it.first))
                    items.addAll(list)
                },
                onError = {
                    loge("Error $it")
                    it.printStackTrace()
                }
            )
    }

    fun loading() = isLoading.get()

    fun refreshList() {

    }

    private fun handleEmptyPage(viewModels: MutableList<BaseModel>): List<BaseModel> {
        if (pagingBody.isFirstPage()) {
            viewModels.add(
                DummyVm(R.layout.empty_state_home)
            )
        }
        return viewModels
    }
}
