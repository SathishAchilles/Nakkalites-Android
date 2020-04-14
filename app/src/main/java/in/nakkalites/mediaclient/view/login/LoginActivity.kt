package `in`.nakkalites.mediaclient.view.login

import `in`.nakkalites.logging.logThrowable
import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivityLoginBinding
import `in`.nakkalites.mediaclient.domain.models.User
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.home.HomeActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.login.LoginVm
import `in`.nakkalites.mediaclient.viewmodel.utils.NoUserFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    val vm: LoginVm by viewModel()

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
                    setupCrashlyticsUserDetails(it.data)
                    goToHome()
                }
                is Result.Error -> {
                    hideLoading()
                    errorHandler(it.throwable) {
                        if (it.throwable is NoUserFoundException) {
                            showError(getString(R.string.error_no_user))
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
        if (!BuildConfig.DEBUG) {
            Crashlytics.setUserIdentifier(user.id)
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
        }
    }

    private fun goToHome() {
        HomeActivity.createIntent(this)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { startActivity(it) }
    }
}
