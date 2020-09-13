package `in`.nakkalites.mediaclient.view.login

import `in`.nakkalites.logging.logThrowable
import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants.Property
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.ActivityLoginBinding
import `in`.nakkalites.mediaclient.domain.models.User
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.home.HomeActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.getTimeStampForAnalytics
import `in`.nakkalites.mediaclient.viewmodel.login.LoginVm
import `in`.nakkalites.mediaclient.viewmodel.utils.NoUserFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    val vm: LoginVm by viewModel()
    val analyticsManager by inject<AnalyticsManager>()
    val crashlytics by inject<FirebaseCrashlytics>()

    companion object {
        private const val RC_SIGN_IN = 9001

        @JvmStatic
        fun createIntent(ctx: Context) =
            Intent(ctx, LoginActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.callback = callback
        setupGoogleSignInOptions()
        hideLoading()
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> {
                    hideLoading()
                    val user = it.data
                    setupCrashlyticsUserDetails(user)
                    trackUserLoggedIn(user)
                    goToHome()
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

    private fun setupCrashlyticsUserDetails(user: User) {
        crashlytics.setUserId(user.id)
        user.email?.let {
            crashlytics.setCustomKey(AppConstants.USER_EMAIL, it)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                vm.login(account)
            } catch (e: Throwable) {
                logThrowable(e)
                Snackbar
                    .make(binding.root, getString(R.string.error_network), Snackbar.LENGTH_SHORT)
                    .show()
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
        binding.signInButton.setOnClickListener { callback.onSignUpClick() }
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
            AnalyticsConstants.Event.SIGN_UP
        } else {
            AnalyticsConstants.Event.LOGIN
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
}
