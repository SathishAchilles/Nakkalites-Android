package `in`.nakkalites.mediaclient.view.login

import `in`.nakkalites.logging.logThrowable
import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants.Property
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.databinding.ActivityLoginBinding
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.domain.models.User
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.home.HomeActivity
import `in`.nakkalites.mediaclient.view.profile.ProfileAddActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.getTimeStampForAnalytics
import `in`.nakkalites.mediaclient.view.utils.showSoftKeyboard
import `in`.nakkalites.mediaclient.viewmodel.login.LoginUtils
import `in`.nakkalites.mediaclient.viewmodel.login.LoginVm
import `in`.nakkalites.mediaclient.viewmodel.utils.NoUserFoundException
import `in`.nakkalites.mediaclient.viewmodel.utils.parsePhoneNumber
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.credentials.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.truecaller.android.sdk.*
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class LoginActivity : BaseActivity(), CountriesBottomSheetCallbacks {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    val vm: LoginVm by viewModel()
    val analyticsManager by inject<AnalyticsManager>()
    val userManager by inject<UserManager>()
    val crashlytics by inject<FirebaseCrashlytics>()
    val phoneNumberUtil by inject<PhoneNumberUtil>()
    private var isAutoInitiated: Boolean = false

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val RESOLVE_HINT = 9002
        private const val PLAY_SERVICES_REQUEST_CODE = 9003

        @JvmStatic
        fun createIntent(ctx: Context) =
            Intent(ctx, LoginActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.callback = callback
        binding.vm = vm
        setupTruecallerOptions(false)
        setupGoogleSignInOptions()
        binding.phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = binding.phoneEditText.text.toString()
                binding.loginCta.isEnabled = text.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        hideLoading()
        vm.viewStates().observe(this, EventObserver {
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
                        if (it.throwable is NoUserFoundException) {
                            showError(getString(R.string.generic_error_message))
                            false
                        } else {
                            true
                        }
                    }
                }
                is Result.Loading -> {
                    showLoading()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        TruecallerSDK.clear()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupTruecallerOptions(withOtp: Boolean) {
        val sdkCallback = object : ITrueCallback {
            override fun onSuccessProfileShared(trueProfile: TrueProfile) {
                val phoneNumber = trueProfile.phoneNumber.parsePhoneNumber(phoneNumberUtil)
                vm.loginViaTrueCaller(
                    trueProfile,
                    phoneNumber?.countryCode?.let { "+$it" },
                    phoneNumber?.nationalNumber?.toString()
                )
            }

            override fun onFailureProfileShared(trueError: TrueError) {
                trackTruecallerLoginFailed(trueError.errorType)
                if (isAutoInitiated) {
                    isAutoInitiated = false
                    requestHint()
                } else {
                    vm.loginViaTrueCaller(null)
                }
            }

            override fun onVerificationRequired(trueError: TrueError?) {
            }

        }
        val trueScope = TruecallerSdkScope.Builder(this, sdkCallback)
            .consentMode(TruecallerSdkScope.CONSENT_MODE_BOTTOMSHEET)
            .buttonColor(ContextCompat.getColor(this, R.color.colorAccent))
            .buttonTextColor(ContextCompat.getColor(this, R.color.white))
            .loginTextPrefix(TruecallerSdkScope.LOGIN_TEXT_PREFIX_TO_GET_STARTED)
            .loginTextSuffix(TruecallerSdkScope.LOGIN_TEXT_SUFFIX_PLEASE_LOGIN_SIGNUP)
            .ctaTextPrefix(TruecallerSdkScope.CTA_TEXT_PREFIX_CONTINUE_WITH)
            .buttonShapeOptions(TruecallerSdkScope.BUTTON_SHAPE_ROUNDED)
            .privacyPolicyUrl(HttpConstants.PRIVACY_POLICY)
            .termsOfServiceUrl(HttpConstants.TERMS_CONDITIONS)
            .footerType(TruecallerSdkScope.FOOTER_TYPE_NONE)
            .consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_LOG_IN)
            .sdkOptions(
                if (withOtp) {
                    TruecallerSdkScope.SDK_OPTION_WITH_OTP
                } else {
                    TruecallerSdkScope.SDK_OPTION_WITHOUT_OTP
                }
            )
            .build()
        TruecallerSDK.init(trueScope)
        vm.showTruecallerButton.set(TruecallerSDK.getInstance().isUsable)
    }

    private fun startTruecallerLogin() {
        if (TruecallerSDK.getInstance().isUsable) {
            vm.showTruecallerButton.set(true)
            try {
                TruecallerSDK.getInstance().getUserProfile(this)
            } catch (e: Exception) {
                Timber.e(e)
                requestHint()
            }
        } else {
            trackTruecallerNotPresent()
            vm.showTruecallerButton.set(false)
            requestHint()
        }
    }

    private fun setupCrashlyticsUserDetails(user: User) {
        crashlytics.setUserId(user.id)
        user.email?.let {
            crashlytics.setCustomKey(AppConstants.USER_EMAIL, it)
        }
        user.phoneNumber?.let {
            crashlytics.setCustomKey(AppConstants.USER_PHONE, it)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun requestHint() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val options = CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build()
        val credentialsClient = Credentials.getClient(applicationContext, options)
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        try {
            startIntentSenderForResult(intent.intentSender, RESOLVE_HINT, null, 0, 0, 0, Bundle())
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                vm.loginViaGoogle(account)
            } catch (e: Throwable) {
                logThrowable(e)
                Snackbar
                    .make(binding.root, getString(R.string.error_network), Snackbar.LENGTH_SHORT)
                    .show()
            }
        } else if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                val credential = data?.getParcelableExtra(Credential.EXTRA_KEY) as? Credential
                val phoneNumber = credential?.id?.parsePhoneNumber(phoneNumberUtil)
                val nationalNumber = phoneNumber?.run { nationalNumber.toString() }
                if (nationalNumber != null) {
                    vm.onHintSelected(phoneNumber)
                    binding.phoneEditText.setText(nationalNumber)
                    binding.phoneEditText.setSelection(nationalNumber.length)
                }
            } else if (resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
                binding.phoneEditText.requestLayout()
                showSoftKeyboard(binding.phoneEditText)
            }
        } else if (requestCode == TruecallerSDK.SHARE_PROFILE_REQUEST_CODE) {
            setupTruecallerOptions(false)
            try {
                TruecallerSDK.getInstance()
                    .onActivityResultObtained(this, requestCode, resultCode, data)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun setupGoogleSignInOptions() {
        setGoogleButtonText(binding.signInButton, getString(R.string.continue_with_google))
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.signInButton.setOnClickListener {
            trackGoogleCTAClicked()
            callback.onSignUpClick()
        }
        isAutoInitiated = true
        startTruecallerLogin()
    }

    private fun setGoogleButtonText(signInButton: SignInButton, buttonText: String) {
        for (i in 0 until signInButton.childCount) {
            val view: View = signInButton.getChildAt(i)
            if (view is TextView) {
                view.text = buttonText
                return
            }
        }
    }

    private val callback = object : LoginViewCallbacks {
        override fun onSignUpClick() {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            trackSignUpClicked()
        }

        override fun onSignInWithPhoneNumberClick() {
            val nationalNumber = vm.phoneNumber?.nationalNumber
            val enteredPhoneNumber = binding.phoneEditText.text
            if (nationalNumber != null || enteredPhoneNumber.isNotEmpty()) {
                if (isPlayServicesAvailable(this@LoginActivity)) {
                    OtpVerificationActivity.createIntent(
                        this@LoginActivity, vm.countryCodeVm.phoneCode,
                        nationalNumber?.toString() ?: enteredPhoneNumber.toString()
                    ).also { startActivity(it) }
//                } else if (vm.countryCodeVm.phoneCode == "+91") {
//                    setupTruecallerOptions(withOtp = true)
//                    val apiCallback = object : VerificationCallback {
//                        override fun onRequestSuccess(
//                            requestCode: Int,
//                            extras: VerificationDataBundle?
//                        ) {
//                            if (requestCode == VerificationCallback.TYPE_MISSED_CALL_INITIATED) {
//                                extras?.getString(VerificationDataBundle.KEY_TTL)
//
//                            }
//                            if (requestCode == VerificationCallback.TYPE_MISSED_CALL_RECEIVED) {
//
//                            }
//                            if (requestCode == VerificationCallback.TYPE_OTP_INITIATED) {
//                                extras?.getString(VerificationDataBundle.KEY_TTL)
//
//                            }
//                            if (requestCode == VerificationCallback.TYPE_OTP_RECEIVED) {
//
//                            }
//                            if (requestCode == VerificationCallback.TYPE_VERIFICATION_COMPLETE) {
//
//                            }
//                            if (requestCode == VerificationCallback.TYPE_PROFILE_VERIFIED_BEFORE) {
//
//                            }
//                        }
//
//                        override fun onRequestFailure(p0: Int, p1: TrueException) {
//                        }
//
//                    }
//                    TruecallerSDK.getInstance().requestVerification(
//                        "IN",
//                        nationalNumber?.toString() ?: enteredPhoneNumber.toString(),
//                        apiCallback,
//                        this@LoginActivity
//                    )
                } else {
                    showPlayServicesUnavailableDialog()
                }
            } else {
                showError(getString(R.string.enter_phone_number_error))
            }
        }

        override fun onSignInWithTruecallerClick() {
            trackTruecallerCTAClicked()
            setupTruecallerOptions(false)
            isAutoInitiated = false
            startTruecallerLogin()
        }

        override fun onFlagClick() {
            val countries = vm.countryCodeVm
                .getCountriesListForBottomSheet(resources.getStringArray(R.array.country_codes_data))
            val sheet = CountriesBottomSheet.newInstance(countries, true)
            sheet.showAllowingStateLoss(supportFragmentManager)
        }

        override fun onTnCClick() {
            Intent(
                Intent.ACTION_VIEW, Uri.parse(HttpConstants.TERMS_CONDITIONS)
            ).let(::startActivity)
        }

        override fun onPrivacyPolicyClick() {
            Intent(
                Intent.ACTION_VIEW, Uri.parse(HttpConstants.PRIVACY_POLICY)
            ).let(::startActivity)
        }
    }

    private fun goToProfileAdd() {
        ProfileAddActivity.createIntent(this)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { startActivity(it) }
    }

    private fun goToHome() {
        HomeActivity.createIntent(this)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { startActivity(it) }
    }

    private fun trackSignUpClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.SIGN_UP_CTA_CLICKED)
    }

    private fun trackUserLoggedIn(user: User) {
        analyticsManager.setUserId(user.id)
        analyticsManager.logUserProperty(Property.USER_PHONE, user.phoneNumber)
        analyticsManager.logUserProperty(Property.USER_EMAIL, user.email)
        analyticsManager.logUserProperty(Property.USER_ID, user.id)
        analyticsManager.logUserProperty(Property.USER_IMAGE_URL, user.imageUrl)
        analyticsManager.logUserProperty(Property.USER_NAME, user.name)
        val bundle = Bundle().apply {
            putString(Property.USER_EMAIL, user.email)
            putString(Property.USER_ID, user.id)
            putString(Property.USER_IMAGE_URL, user.imageUrl)
            putString(Property.USER_NAME, user.name)
            putString(FirebaseAnalytics.Param.METHOD, "google_sso")
        }
        val eventName = if (user.isFirstLogin) {
            FirebaseAnalytics.Event.SIGN_UP
        } else {
            FirebaseAnalytics.Event.LOGIN
        }
        if (user.isFirstLogin) {
            analyticsManager.logUserProperty(
                Property.FIRST_SIGN_UP_DATE, getTimeStampForAnalytics()
            )
        }
        analyticsManager.logEvent(eventName, bundle)
    }

    private fun trackLoginFailed() {
        analyticsManager.logEvent(AnalyticsConstants.Event.LOGIN_FAILED)
    }

    private fun trackTruecallerLoginFailed(errorCode: Int) {
        val bundle = Bundle().apply {
            putInt(Property.ERROR_CODE, errorCode)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.TRUECALLER_LOGIN_FAILED, bundle)
    }

    private fun trackTruecallerNotPresent() {
        analyticsManager.logEvent(AnalyticsConstants.Event.TRUECALLER_NOT_PRESENT)
    }

    private fun trackTruecallerCTAClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.TRUECALLER_CTA_CLICKED)
    }

    private fun trackGoogleCTAClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.GOOGLE_CTA_CLICKED)
    }

    override fun onCountrySelected(position: Int, withFlags: Boolean) {
        val countriesList = resources.getStringArray(R.array.country_codes_data).asList()
        vm.countryCodeVm.selectCountry(countriesList, position)
    }

    fun showPlayServicesUnavailableDialog() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (googleApiAvailability.isUserResolvableError(status)) {
            googleApiAvailability.getErrorDialog(this, status, PLAY_SERVICES_REQUEST_CODE).show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.google_play_services_unavailable)
                .setMessage(R.string.google_play_services_unavailable_desc)
                .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    fun isPlayServicesAvailable(context: Context) =
        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
}
