package `in`.nakkalites.mediaclient.view.login

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.ActivityOtpVerificationBinding
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.domain.models.User
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.home.HomeActivity
import `in`.nakkalites.mediaclient.view.profile.ProfileAddActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.getTimeStampForAnalytics
import `in`.nakkalites.mediaclient.viewmodel.login.LoginUtils
import `in`.nakkalites.mediaclient.viewmodel.login.OtpVerificationVm
import `in`.nakkalites.mediaclient.viewmodel.utils.NoUserFoundException
import `in`.nakkalites.mediaclient.viewmodel.utils.PhoneAuthException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


class OtpVerificationActivity : BaseActivity(), OtpReceivedInterface, OtpVerificationCallbacks {
    private lateinit var binding: ActivityOtpVerificationBinding
    private val otpVerificationVm: OtpVerificationVm by viewModel()
    val crashlytics by inject<FirebaseCrashlytics>()
    val analyticsManager by inject<AnalyticsManager>()
    val userManager by inject<UserManager>()

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var smsBroadcastReceiver: SmsBroadcastReceiver? = null
    private var resendOtpTextDisposable: Disposable? = null
    private val countryCode: String by lazy {
        intent.getStringExtra(AppConstants.COUNTRY_CODE) ?: AppConstants.AppCountry.DIALING_CODE
    }
    private val phoneNumber: String by lazy {
        intent.getStringExtra(AppConstants.PHONE_NUMBER)!!
    }

    companion object {

        @JvmStatic
        fun createIntent(ctx: Context, countryCode: String, phoneNumber: String) =
            Intent(ctx, OtpVerificationActivity::class.java)
                .putExtra(AppConstants.COUNTRY_CODE, countryCode)
                .putExtra(AppConstants.PHONE_NUMBER, phoneNumber)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otp_verification)
        if (otpVerificationVm.storedVerificationId == null && savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }
        binding.vm = otpVerificationVm
        binding.callbacks = this
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        otpVerificationVm.setArgs(countryCode, phoneNumber)
        initiateSMSListener()
        startPhoneNumberVerification(countryCode + phoneNumber)
        otpVerificationVm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> {
                    hideLoading()
                    val user = it.data
                    setupCrashlyticsUserDetails(user)
                    trackUserLoggedIn(user)
                    if (LoginUtils.shouldShowProfileAddPage(userManager)) {
                        goToProfileAdd()
                    } else {
                        goToHome()
                    }
                }
                is Result.Error -> {
                    trackLoginFailed()
                    hideLoading()
                    errorHandler(it.throwable) {
                        loge("Login Failure", throwable = it.throwable)
                        val error = it.throwable
                        val unwrappedError =
                            if (error is ExecutionException) error.cause ?: error else error
                        when {
                            otpVerificationVm.storedVerificationId != null -> {
                                otpVerificationVm.onOtpError(
                                    PhoneAuthException.mapFirebaseException(
                                        unwrappedError
                                    ), otpVerificationVm.otpCode
                                )
                                true
                            }
                            it.throwable is NoUserFoundException -> {
                                showError(getString(R.string.generic_error_message))
                                false
                            }
                            else -> {
                                true
                            }
                        }
                    }
                }
                is Result.Loading -> {
                    showLoading()
                }
            }
        })
        countdownToEnableResend()
    }

    override fun onSaveInstanceState(@NonNull outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(AppConstants.KEY_VERIFICATION_ID, otpVerificationVm.storedVerificationId)
    }

    override fun onRestoreInstanceState(@NonNull savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        otpVerificationVm.storedVerificationId =
            savedInstanceState.getString(AppConstants.KEY_VERIFICATION_ID)
    }

    private fun initiateSMSListener() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver?.setOnOtpListeners(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        applicationContext.registerReceiver(smsBroadcastReceiver, intentFilter)
        startSMSListener()
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                otpVerificationVm.signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Timber.e(e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
                Toast.makeText(
                    this@OtpVerificationActivity, PhoneAuthException.mapFirebaseException(e).resId,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onCodeSent(
                verificationId: String, token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                // Save verification ID and resending token so we can use them later
                otpVerificationVm.storedVerificationId = verificationId
                otpVerificationVm.resendToken = token
                otpVerificationVm.onOtpSent()
            }
        }
        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun countdownToEnableResend() {
        resendOtpTextDisposable?.dispose()
        resendOtpTextDisposable = otpVerificationVm.countdownForResendOtp()
            .map { getString(R.string.resend_otp_in_x_secs, it) }
            .doOnSubscribe { binding.isResendEnabled = false }
            .subscribeBy(
                onNext = { binding.resendOtpText = it },
                onComplete = {
                    binding.resendOtpText = getString(R.string.didn_t_receive_otp_resend)
                    binding.isResendEnabled = true
                }
            )
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        try {
            PhoneAuthProvider.getCredential(verificationId!!, code)
        } catch (e: Exception) {
            binding.resendOtpText = getString(R.string.didn_t_receive_otp_resend)
            binding.isResendEnabled = true
            Toast.makeText(this, getString(R.string.oops_something_went_wrong), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        countdownToEnableResend()
        val optionsBuilder = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private fun startSMSListener() {
        val mClient = SmsRetriever.getClient(this)
        mClient.startSmsRetriever()
    }

    override fun onOtpReceived(otp: String?) {
        if (otp != null) {
            otpVerificationVm.otpCode = otp
            verifyPhoneNumberWithCode(otpVerificationVm.storedVerificationId, otp)
        }
    }

    override fun onOtpTimeout() {
        Toast.makeText(this, getString(R.string.otp_read_timeout), Toast.LENGTH_SHORT).show()
    }

    override fun onVerifyClick() {
        trackVerifyClicked()
        verifyPhoneNumberWithCode(otpVerificationVm.storedVerificationId, otpVerificationVm.otpCode)
    }

    override fun onResendClick() {
        trackResendClicked()
        resendVerificationCode(countryCode + phoneNumber, otpVerificationVm.resendToken)
    }

    override fun onDestroy() {
        super.onDestroy()
        smsBroadcastReceiver?.run { applicationContext.unregisterReceiver(this) }
        resendOtpTextDisposable?.dispose()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun setupCrashlyticsUserDetails(user: User) {
        crashlytics.setUserId(user.id)
        user.email?.let {
            crashlytics.setCustomKey(AppConstants.USER_EMAIL, it)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun trackUserLoggedIn(user: User) {
        analyticsManager.setUserId(user.id)
        analyticsManager.logUserProperty(AnalyticsConstants.Property.USER_EMAIL, user.email)
        analyticsManager.logUserProperty(AnalyticsConstants.Property.USER_PHONE, user.phoneNumber)
        analyticsManager.logUserProperty(AnalyticsConstants.Property.USER_ID, user.id)
        analyticsManager.logUserProperty(AnalyticsConstants.Property.USER_IMAGE_URL, user.imageUrl)
        analyticsManager.logUserProperty(AnalyticsConstants.Property.USER_NAME, user.name)
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.USER_EMAIL, user.email)
            putString(AnalyticsConstants.Property.USER_PHONE, user.phoneNumber)
            putString(AnalyticsConstants.Property.USER_ID, user.id)
            putString(AnalyticsConstants.Property.USER_IMAGE_URL, user.imageUrl)
            putString(AnalyticsConstants.Property.USER_NAME, user.name)
            putString(FirebaseAnalytics.Param.METHOD, "firebase_otp")
        }
        val eventName = if (user.isFirstLogin) {
            FirebaseAnalytics.Event.SIGN_UP
        } else {
            FirebaseAnalytics.Event.LOGIN
        }
        if (user.isFirstLogin) {
            analyticsManager.logUserProperty(
                AnalyticsConstants.Property.FIRST_SIGN_UP_DATE, getTimeStampForAnalytics()
            )
        }
        analyticsManager.logEvent(eventName, bundle)
    }

    private fun trackLoginFailed() {
        analyticsManager.logEvent(AnalyticsConstants.Event.LOGIN_FAILED)
    }

    private fun trackVerifyClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.VERIFY_OTP_CLICKED)
    }

    private fun trackResendClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.RESEND_OTP_CLICKED)
    }

    private fun goToHome() {
        HomeActivity.createIntent(this)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { startActivity(it) }
    }

    private fun goToProfileAdd() {
        ProfileAddActivity.createIntent(this)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { startActivity(it) }
    }
}

interface OtpVerificationCallbacks {
    fun onVerifyClick()
    fun onResendClick()
}
