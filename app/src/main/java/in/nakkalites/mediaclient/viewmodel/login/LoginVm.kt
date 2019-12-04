package `in`.nakkalites.mediaclient.viewmodel.login

import `in`.nakkalites.mediaclient.domain.login.LoginDomain
import `in`.nakkalites.mediaclient.domain.models.User
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.asResult
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.NoUserFoundException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class LoginVm(private val loginDomain: LoginDomain) : BaseViewModel() {
    private val loginState = MutableLiveData<Event<Result<User>>>()

    fun viewStates(): LiveData<Event<Result<User>>> = loginState

    fun login(account: GoogleSignInAccount?) {
        if (account != null) {
            disposables += loginDomain.login(
                account.id!!, account.displayName!!, account.email!!, account.photoUrl
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .asResult(Result.Loading())
                .subscribeBy(
                    onNext = {
                        loginState.value = Event(it)
                    },
                    onError = {
                        loginState.value = Event(it as Result.Error<User>)
                    }
                )
        } else {
            loginState.value = Event(Result.Error<User>(null,
                NoUserFoundException()
            ))
        }
    }
}
