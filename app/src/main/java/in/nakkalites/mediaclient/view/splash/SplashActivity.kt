package `in`.nakkalites.mediaclient.view.splash

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivitySplashBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.home.HomeActivity
import `in`.nakkalites.mediaclient.view.login.LoginActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.splash.SplashVm
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    val vm: SplashVm by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> goToHome()
                is Result.Error -> goToLogin()
                else -> showLoading()
            }
        })
        vm.updateViewState()
    }

    private fun goToLogin() {
        startActivity(LoginActivity.createIntent(this))
    }

    private fun goToHome() {
        startActivity(HomeActivity.createIntent(this))
    }

    private fun showLoading() {

    }
}
