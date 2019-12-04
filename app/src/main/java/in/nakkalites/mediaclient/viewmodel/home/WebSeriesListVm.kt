package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.domain.utils.PagingBody
import `in`.nakkalites.mediaclient.domain.utils.PagingCallback
import androidx.databinding.ObservableBoolean

class WebSeriesListVm : BaseViewModel() {
    internal val isRefreshing = ObservableBoolean()
    internal val isLoading = ObservableBoolean()
    private lateinit var pagingBody: PagingBody

    internal fun initPagingBody(pagingCallback: PagingCallback) {
        pagingBody = PagingBody(pagingCallback = pagingCallback)
    }

    fun refreshList() {

    }
}
