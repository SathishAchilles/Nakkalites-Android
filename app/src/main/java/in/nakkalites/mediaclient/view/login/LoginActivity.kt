package `in`.nakkalites.mediaclient.view.login

import `in`.nakkalites.logging.logThrowable
import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivityLoginBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.home.HomeActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.login.LoginVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

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
        binding.onSignUpClick = onSignUpClick
        setupGoogleSignInOptions()
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> goToHome()
                is Result.Error -> {
                    Timber.e(it.throwable)
                    showError(it.throwable.message)
                }
            }
        })
    }

    private fun showError(message: String?) {
        Toast.makeText(this, message ?: "Error", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                vm.login(account)
                loge("Logged in ${account?.email} ${account?.displayName} ${account?.account}")
            } catch (e: ApiException) {
                logThrowable(e)
            }
        }
    }

    private fun setupGoogleSignInOptions() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private val onSignUpClick = {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun goToHome() {
        startActivity(HomeActivity.createIntent(this))
    }
}