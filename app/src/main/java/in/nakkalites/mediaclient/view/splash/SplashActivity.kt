package `in`.nakkalites.mediaclient.view.splash

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivitySplashBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.home.HomeActivity
import `in`.nakkalites.mediaclient.view.login.LoginActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.splash.SplashVm
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
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
        LoginActivity.createIntent(this)
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

    private fun showLoading() {
        vm.isAnimating = true
        splashAnimator(binding.logoGroup)
    }

    private fun splashAnimator(view: View) {
        val alpha = 1f
        val scaleX = 1f
        val scaleY = 1f
        val startScaleX = 3f
        val startScaleY = 3f
        val startAlpha = 0f
        val duration = 1000L
        val factor = 2f
        view.scaleX = scaleX
        view.scaleY = scaleY
        view.alpha = alpha
        view.animate()
            .setInterpolator(LinearInterpolator())
            .alpha(startAlpha)
//            .scaleX(startScaleX)
//            .scaleY(startScaleY)
            .setDuration(duration)
            .withEndAction { vm.setAnimationEnded() }
    }
}
