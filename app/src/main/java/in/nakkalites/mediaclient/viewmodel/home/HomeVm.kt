package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesListVm
import androidx.databinding.ObservableArrayList

class HomeVm(
    val userManager: UserManager, private val allVideoGroupsVm: AllVideoGroupsVm,
    private val webSeriesListVm: WebSeriesListVm
) : BaseViewModel()

