package `in`.nakkalites.mediaclient.viewmodel.webseries

import `in`.nakkalites.mediaclient.R
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
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class WebSeriesDetailVm(private val videoGroupDomain: VideoGroupDomain) : BaseViewModel() {
    val items = ObservableArrayList<BaseModel>()
    val pageTitle = ObservableField<String>()
    private val isDataLoading = ObservableBoolean()
    var id: String? = null
    var name: String? = null
    var thumbnail: String? = null
    private val viewState = MutableLiveData<Event<Result<Unit>>>()

    fun viewStates(): LiveData<Event<Result<Unit>>> = viewState

    fun setArgs(id: String, name: String, thumbnail: String) {
        this.id = id
        this.name = name
        this.thumbnail = thumbnail
        pageTitle.set(name)
    }

    fun fetchWebSeriesDetail(id: String) {
        disposables += videoGroupDomain.getWebSeriesDetail(id)
            .map { webSeries ->
                listOf(WebSeriesDetailItemVm(webSeries)) +
                        webSeries.videoGroups.map { VideoGroupVm(it) }
            }
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


    private fun handleEmptyPage(viewModels: MutableList<BaseModel>): List<BaseModel> {
        if (viewModels.isEmpty()) {
            viewModels.add(EmptyStateVm(R.layout.empty_state))
        }
        return viewModels
    }
}
