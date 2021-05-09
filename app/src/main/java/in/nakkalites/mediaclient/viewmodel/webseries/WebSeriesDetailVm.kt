package `in`.nakkalites.mediaclient.viewmodel.webseries

import `in`.nakkalites.logging.logd
import `in`.nakkalites.mediaclient.domain.models.Season
import `in`.nakkalites.mediaclient.domain.models.WebSeries
import `in`.nakkalites.mediaclient.domain.subscription.PlanManager
import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.RxTransformers
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class WebSeriesDetailVm(private val videoGroupDomain: VideoGroupDomain, planManager: PlanManager) :
    BaseViewModel() {
    val items = ObservableArrayList<BaseModel>()
    val pageTitle = ObservableField<String>()
    private val isDataLoading = ObservableBoolean()
    var id: String? = null
    var name: String? = null
    var thumbnail: String? = null
    private val seasons = mutableListOf<Season>()
    private var selectedSeasonId: String? = null
    private val viewState = MutableLiveData<Event<Result<Unit>>>()

    init {
        disposables += planManager.getPlanObserver()
            .filter { it }
            .observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                fetchWebSeriesDetail(id!!)
                logd("Refresh Page")
            })
    }

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
                seasons.addAll(webSeries.seasons)
                createViewModels(webSeries)
            }
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

    private fun createViewModels(webSeries: WebSeries): List<BaseModel> {
        val viewModels = mutableListOf<BaseModel>(WebSeriesDetailItemVm(webSeries))
        viewModels.add(SeasonHeaderVm(webSeries.seasons))
        viewModels.addAll(createEpisodeVms(webSeries.seasons.firstOrNull()))
        return viewModels
    }

    private fun createEpisodeVms(season: Season?): List<BaseModel> {
        val viewModels = mutableListOf<BaseModel>()
        selectedSeasonId = season?.id
        season?.episodes?.mapIndexed { index, video ->
            viewModels.add(SeasonEpisodeItemVm(season.id, season.name, index + 1, video))
        }
        return viewModels
    }

    fun onSeasonSelected(seasonPair: Pair<String, String>) {
        if (selectedSeasonId != seasonPair.first) {
            items.removeAll { it is SeasonEpisodeItemVm }
            items.addAll(createEpisodeVms(seasons.firstOrNull { it.id == seasonPair.first }))
        }
    }

}
