package `in`.nakkalites.mediaclient.viewmodel.splash

import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.NoUserFoundException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class SplashVm(private val userManager: UserManager) : BaseViewModel() {

    var isViewAnimating = true
        set(value) {
            field = value
            setAnimationEnded()
        }
    var hasConfigRetrieved = false
        set(value) {
            field = value
            setAnimationEnded()
        }
    private val splashViewState = MutableLiveData<Event<Result<Unit>>>()
    private var result: Result<Unit>? = null

    fun viewStates(): LiveData<Event<Result<Unit>>> = splashViewState

    fun updateViewState() {
        splashViewState.value = Event(Result.Loading())
        result = if (userManager.isUserLoggedIn()) {
            Result.Success(Unit)
        } else {
            Result.Error(Unit, NoUserFoundException())
        }
    }

    private fun setAnimationEnded() {
        if (!isViewAnimating && hasConfigRetrieved) {
            splashViewState.value = Event(result!!)
        }
    }
}
