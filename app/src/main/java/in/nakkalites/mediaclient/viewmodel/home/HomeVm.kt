package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.data.user.UserManager
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.databinding.ObservableArrayList

class HomeVm(val userManager: UserManager) : BaseViewModel() {

    private val tabs: List<HomeTab> = listOf(HomeTab.ALL, HomeTab.WEB_SERIES)
    internal val pages = ObservableArrayList<BaseViewModel>().apply { addAll(createPages()) }
    internal val pageTitleRes = tabs.map {
        when (it) {
            HomeTab.ALL -> R.string.all
            HomeTab.WEB_SERIES -> R.string.web_series
        }
    }
    internal var selectedTab: HomeTab = HomeTab.ALL

    private fun createPages(): List<BaseViewModel> = tabs.map {
        when (it) {
            HomeTab.ALL -> AllVideoGroupsVm()
            HomeTab.WEB_SERIES -> WebSeriesVm()
        }
    }

    fun setSelectedTab(position: Int) {
        selectedTab = HomeTab.fromPosition(position)
    }
}

