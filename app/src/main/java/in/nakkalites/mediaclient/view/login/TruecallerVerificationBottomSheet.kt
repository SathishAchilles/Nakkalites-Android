package `in`.nakkalites.mediaclient.view.login

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.FragmentTruecallerVerificationBottomsheetBinding
import `in`.nakkalites.mediaclient.view.BaseBottomSheetFragment
import `in`.nakkalites.mediaclient.view.utils.commitAllowingStateLoss
import `in`.nakkalites.mediaclient.viewmodel.login.TOTAL_RESEND_COUNTDOWNS
import `in`.nakkalites.mediaclient.viewmodel.login.TruecallerVerificationVm
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.koin.androidx.viewmodel.ext.android.viewModel

class TruecallerVerificationBottomSheet : BaseBottomSheetFragment(),
    TruecallerVerificationBottomSheetCallbacks {
    private lateinit var binding: FragmentTruecallerVerificationBottomsheetBinding
    private val vm: TruecallerVerificationVm by viewModel()
    private var listener: TruecallerVerificationBottomSheetListener? = null

    private var resendOtpTextDisposable: Disposable? = null
    private val countryCode: String by lazy {
        requireArguments().getString(AppConstants.COUNTRY_CODE)
            ?: AppConstants.AppCountry.DIALING_CODE
    }
    private val phoneNumber: String by lazy {
        requireArguments().getString(AppConstants.PHONE_NUMBER)!!
    }
    private val timeout: Long by lazy {
        requireArguments().getLong(TIMEOUT)
    }

    private val verificationType: VerificationType by lazy {
        requireArguments().getSerializable(VERIFICATION_TYPE) as VerificationType
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentTruecallerVerificationBottomsheetBinding.inflate(LayoutInflater.from(context))
        binding.vm = vm
        binding.callbacks = this
        vm.setArgs(countryCode, phoneNumber, verificationType, timeout)
        countdownToEnableResend()
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = requireActivity() as TruecallerVerificationBottomSheetListener
        } catch (e: ClassCastException) {
            error("$context should implement TruecallerVerificationBottomSheetListener")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        resendOtpTextDisposable?.dispose()
    }

    private fun countdownToEnableResend() {
        resendOtpTextDisposable?.dispose()
        resendOtpTextDisposable = vm.countdownForResendOtp()
            .map { getString(R.string.retry_timer, it) }
            .subscribeBy(
                onNext = { binding.resendOtpText = it },
                onComplete = {
                    binding.resendOtpText = getString(R.string.didn_t_receive_otp_resend)
                }
            )
    }

    companion object {
        private const val VERIFICATION_TYPE = "VERIFICATION_TYPE"
        private const val TIMEOUT = "TIMEOUT"
        fun newInstance(
            type: VerificationType,
            countryCode: String,
            phoneNumber: String,
            timeout: Long?
        ): TruecallerVerificationBottomSheet {
            val args = Bundle().apply {
                putString(AppConstants.COUNTRY_CODE, countryCode)
                putString(AppConstants.PHONE_NUMBER, phoneNumber)
                putSerializable(VERIFICATION_TYPE, type)
                putLong(TIMEOUT, timeout ?: TOTAL_RESEND_COUNTDOWNS)
            }

            return TruecallerVerificationBottomSheet().apply { arguments = args }
        }
    }


    override fun onOtpSubmitted(otp: String) {
        vm.otpCode = otp
        listener?.onOtpSubmitted(otp)
    }

    override fun onVerifyClick() {
        listener?.onOtpSubmitted(vm.otpCode)
    }

    fun showAllowingStateLoss(fm: FragmentManager) {
        commitAllowingStateLoss(this, fm, "truecaller-sheet")
    }

}

enum class VerificationType {
    CALL, OTP
}

interface TruecallerVerificationBottomSheetCallbacks {
    fun onOtpSubmitted(otp: String)
    fun onVerifyClick()
}

interface TruecallerVerificationBottomSheetListener {
    fun onOtpSubmitted(otp: String)
}