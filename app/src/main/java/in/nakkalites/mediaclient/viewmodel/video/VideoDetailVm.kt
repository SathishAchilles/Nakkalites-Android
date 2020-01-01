package `in`.nakkalites.mediaclient.viewmodel.video

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.videogroups.VideoGroupDomain
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.EmptyStateVm
import `in`.nakkalites.mediaclient.viewmodel.utils.RxTransformers
import `in`.nakkalites.mediaclient.viewmodel.utils.toCamelCase
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupVm
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class VideoDetailVm(private val videoGroupDomain: VideoGroupDomain) : BaseViewModel() {
    val items = ObservableArrayList<BaseModel>()
    private val isDataLoading = ObservableBoolean()
    var id: String? = null
    var name: String? = null
    var thumbnail: String? = null
    var url: String? = null
    val pageTitle = ObservableField<String>()

    fun setArgs(id: String, name: String, thumbnail: String, url: String) {
        this.id = id
        this.name = name
        this.thumbnail = thumbnail
        this.url = url
        pageTitle.set(name.toCamelCase())
    }

    fun fetchVideoDetail(id: String) {
        disposables += videoGroupDomain.getVideoDetail(id)
            .map { video ->
                listOf(VideoDetailItemVm(video)) +
                        video.videoGroups
                            .map { VideoGroupVm(it) }
            }
            .map { handleEmptyPage(it.toMutableList()) }
            .observeOn(AndroidSchedulers.mainThread())
            .compose(RxTransformers.dataLoading(isDataLoading, items))
            .subscribeBy(
                onSuccess = {
                    items.addAll(it)
                },
                onError = Timber::e
            )
    }


    private fun handleEmptyPage(viewModels: MutableList<BaseModel>): List<BaseModel> {
        if (viewModels.isEmpty()) {
            viewModels.add(EmptyStateVm(R.layout.empty_state))
        }
        return viewModels
    }

}
