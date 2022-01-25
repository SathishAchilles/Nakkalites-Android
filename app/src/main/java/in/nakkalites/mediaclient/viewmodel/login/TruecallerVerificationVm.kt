package `in`.nakkalites.mediaclient.viewmodel.login

import `in`.nakkalites.mediaclient.view.login.VerificationType
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.utils.timeUnit
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.kizitonwose.time.seconds
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

const val TOTAL_RESEND_COUNTDOWNS = 120L

class TruecallerVerificationVm : BaseViewModel() {
    private var timeout :Long = TOTAL_RESEND_COUNTDOWNS
    val showCallerView = ObservableBoolean()
    val showClose = ObservableBoolean(false)
    val formattedPhoneNumber = ObservableField<String>()
    val otpErrorText = ObservableField<DisplayText>()
    val otpCodePart1 = ObservableField("")
    val otpCodePart2 = ObservableField("")
    val otpCodePart3 = ObservableField("")
    val otpCodePart4 = ObservableField("")
    val otpCodePart5 = ObservableField("")
    val otpCodePart6 = ObservableField("")
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

    fun setArgs(countryCode: String, phoneNumber: String, verificationType: VerificationType, timeout : Long) {
        formattedPhoneNumber.set("$countryCode $phoneNumber")
        showCallerView.set(verificationType == VerificationType.CALL)
        this.timeout = timeout
    }

    fun countdownForResendOtp(): Observable<Int> {
        val interval = 1.seconds
        return Observable.intervalRange(
            0,
            timeout, 0, interval.longValue, interval.timeUnit(),
            AndroidSchedulers.mainThread()
        ).map { (timeout - it - 1).toInt() }
    }
}