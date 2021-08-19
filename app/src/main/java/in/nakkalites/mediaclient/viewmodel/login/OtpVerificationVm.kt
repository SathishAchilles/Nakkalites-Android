package `in`.nakkalites.mediaclient.viewmodel.login

import `in`.nakkalites.mediaclient.domain.login.LoginDomain
import `in`.nakkalites.mediaclient.domain.models.User
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.utils.PhoneAuthException
import `in`.nakkalites.mediaclient.viewmodel.utils.timeUnit
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.kizitonwose.time.seconds
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.ExecutionException

class OtpVerificationVm(val loginDomain: LoginDomain) : BaseViewModel() {
    private val TOTAL_RESEND_COUNTDOWNS = 60L
    private lateinit var phoneNumber: String
    private lateinit var countryCode: String
    private val loginState = MutableLiveData<Event<Result<User>>>()
    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    val formattedPhoneNumber = ObservableField<String>()
    val otpErrorText = ObservableField<DisplayText>()
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
                onError = { error ->
                    var unwrappedError =
                        if (error is ExecutionException) error.cause ?: error else error
                    unwrappedError = if (error is ExecutionException) {
                        PhoneAuthException.mapFirebaseException(unwrappedError)
                    } else {
                        PhoneAuthException.mapFirebaseException(unwrappedError)
                    }
                    loginState.value = Event(Result.Error<User>(null, unwrappedError))
                }
            )
    }

    fun countdownForResendOtp(): Observable<Int> {
        val interval = 1.seconds
        return Observable.intervalRange(
            0,
            TOTAL_RESEND_COUNTDOWNS, 0, interval.longValue, interval.timeUnit(),
            AndroidSchedulers.mainThread()
        ).map { (TOTAL_RESEND_COUNTDOWNS - it - 1).toInt() }
    }

    fun onOtpError(error: PhoneAuthException, otp: String?) {
        val errorMsg = createErrorMessage(error)
        otpErrorText.set(errorMsg)
    }

    private fun createErrorMessage(e: PhoneAuthException) = DisplayText.Singular(e.resId)

    fun onOtpSent() {
        otpErrorText.set(null)
    }
}
