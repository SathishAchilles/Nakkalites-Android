package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.logging.logd
import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.domain.subscription.PlanManager
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesListVm
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class HomeVm(
    userManager: UserManager, private val allVideoGroupsVm: AllVideoGroupsVm,
    private val webSeriesListVm: WebSeriesListVm, planManager: PlanManager
) : BaseViewModel() {

    init {
        disposables += userManager.getUserProfile()
            .observeOn(mainThread())
            .subscribeBy(
                onSuccess = { logd(message = "Profile updated") },
                onError = { throwable ->
                    loge(throwable = throwable, message = "Profile update failed")
                })

        disposables += planManager.getPlans()
            .observeOn(mainThread())
            .subscribeBy(
                onSuccess = { logd(message = "Plans updated") },
                onError = { throwable ->
                    loge(throwable = throwable, message = "Plans update failed")
                })
    }
}

