package `in`.nakkalites.mediaclient.viewmodel.login

import `in`.nakkalites.mediaclient.domain.login.LoginDomain
import `in`.nakkalites.mediaclient.domain.models.User
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.getIsoCode
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.NoUserFoundException
import android.graphics.Paint
import androidx.core.graphics.PaintCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class LoginVm(private val loginDomain: LoginDomain, private val phoneNumberUtil: PhoneNumberUtil) :
    BaseViewModel() {
    private val loginState = MutableLiveData<Event<Result<User>>>()
    val countryCodeVm = CountryCodeVm(flagGlyphChecker = glyphChecker)

    var phoneNumber: Phonenumber.PhoneNumber? = null

    fun viewStates(): LiveData<Event<Result<User>>> = loginState

    fun login(account: GoogleSignInAccount?) {
        if (account != null) {
            loginState.value = Event(Result.Loading())
            disposables += loginDomain.loginViaGoogle(
                account.id!!, account.displayName, account.email!!, account.photoUrl
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        loginState.value = Event(Result.Success(it))
                    },
                    onError = {
                        loginState.value = Event(Result.Error<User>(null, it))
                    }
                )
        } else {
            loginState.value = Event(Result.Error<User>(null, NoUserFoundException()))
        }
    }

    fun onHintSelected(phoneNumber: Phonenumber.PhoneNumber) {
        this.phoneNumber = phoneNumber
        this.countryCodeVm.phoneCode = "+${phoneNumber.countryCode}"
        val isoCode = phoneNumber.getIsoCode(phoneNumberUtil)
        countryCodeVm.setFlagAndPhoneCode(isoCode, countryCodeVm.phoneCode)
    }
}

val glyphChecker: (String) -> Boolean = { code -> PaintCompat.hasGlyph(Paint(), code) }
