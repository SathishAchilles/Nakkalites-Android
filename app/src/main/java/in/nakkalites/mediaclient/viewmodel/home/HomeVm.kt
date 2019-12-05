package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.databinding.ObservableArrayList

class HomeVm(
    val userManager: UserManager, private val allVideoGroupsVm: AllVideoGroupsVm,
    private val webSeriesListVm: WebSeriesListVm
) : BaseViewModel() {

    private val tabs: List<HomeTab> = listOf(HomeTab.ALL, HomeTab.WEB_SERIES)
    internal val pages = ObservableArrayList<BaseViewModel>().apply { addAll(createPages()) }
    internal val pageTitleRes = tabs.map { it.stringRes }
    internal var selectedTab: HomeTab = HomeTab.ALL

    private fun createPages(): List<BaseViewModel> = listOf(allVideoGroupsVm, webSeriesListVm)

    fun setSelectedTab(position: Int) {
        selectedTab = HomeTab.fromPosition(position)
    }
}

