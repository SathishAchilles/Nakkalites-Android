package `in`.nakkalites.mediaclient.viewmodel.splash

import `in`.nakkalites.mediaclient.data.user.UserManager
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.NoUserFoundException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SplashVm(private val userManager: UserManager) : BaseViewModel() {

    private val splashViewState = MutableLiveData<Event<Result<Unit>>>()

    fun viewStates(): LiveData<Event<Result<Unit>>> = splashViewState

    fun updateViewState() {
        splashViewState.value = Event(Result.Loading())
        if (userManager.isUserLoggedIn()) {
            splashViewState.value = Event(Result.Success(Unit))
        } else {
            splashViewState.value = Event(Result.Error(Unit, NoUserFoundException()))
        }
    }
}
