package `in`.nakkalites.mediaclient.view.home

import `in`.nakkalites.logging.logd
import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.ObservableString
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class UserProfileVm(val userManager: UserManager) : BaseViewModel() {

    private var user = userManager.getUser()
    val name = ObservableString(user?.let {
        it.name ?: it.phoneNumber ?: it.email ?: ""
    } ?: "")

    fun fetchProfile() {
        disposables += userManager.getUserProfile()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    user = it
                    logd(message = "Profile updated")
                },
                onError = { throwable ->
                    loge(throwable = throwable, message = "Profile update failed")
                })
    }
}
