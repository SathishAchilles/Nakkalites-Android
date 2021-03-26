package `in`.nakkalites.mediaclient.viewmodel.login

import `in`.nakkalites.mediaclient.domain.login.LoginDomain
import `in`.nakkalites.mediaclient.domain.models.User
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class OtpVerificationVm(val loginDomain: LoginDomain) : BaseViewModel() {
    private lateinit var phoneNumber: String
    private lateinit var countryCode: String
    private val loginState = MutableLiveData<Event<Result<User>>>()
    var storedVerificationId: String? = ""
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    val formattedPhoneNumber = ObservableField<String>()
    val otpCodePart1 = ObservableField<String>("")
    val otpCodePart2 = ObservableField<String>("")
    val otpCodePart3 = ObservableField<String>("")
    val otpCodePart4 = ObservableField<String>("")
    val otpCodePart5 = ObservableField<String>("")
    val otpCodePart6 = ObservableField<String>("")
    var otpCode: String
        get() = otpCodePart1.get() + otpCodePart2.get() + otpCodePart3.get() + otpCodePart4.get() +
                otpCodePart5.get() + otpCodePart6.get()
        set(value) {
            otpCodePart1.set(value[0].toString())
            otpCodePart2.set(value[1].toString())
            otpCodePart3.set(value[2].toString())
            otpCodePart4.set(value[3].toString())
            otpCodePart5.set(value[4].toString())
            otpCodePart6.set(value[5].toString())
        }

    fun setArgs(countryCode: String, phoneNumber: String) {
        this.countryCode = countryCode
        this.phoneNumber = phoneNumber
        formattedPhoneNumber.set("$countryCode $phoneNumber")
    }

    fun viewStates(): LiveData<Event<Result<User>>> = loginState

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        loginState.value = Event(Result.Loading())
        disposables += loginDomain.loginViaFirebaseOtp(countryCode, phoneNumber)
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

    }
}
