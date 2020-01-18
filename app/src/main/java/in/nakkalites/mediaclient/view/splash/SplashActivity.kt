package `in`.nakkalites.mediaclient.view.splash

import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivitySplashBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.home.HomeActivity
import `in`.nakkalites.mediaclient.view.login.LoginActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.playStoreIntent
import `in`.nakkalites.mediaclient.viewmodel.splash.SplashVm
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.databinding.DataBindingUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    val vm: SplashVm by viewModel()
    val remoteConfig by inject<FirebaseRemoteConfig>()

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
        fetchConfig()
        vm.updateViewState()
    }

    override fun onResume() {
        super.onResume()
        updateConfig()
    }

    private fun fetchConfig() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.default_remote_configs)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                    updateConfig()
                else
                    vm.hasConfigRetrieved = true
            }
    }

    private fun updateConfig() {
        val currentVersion = BuildConfig.VERSION_CODE
        val latestVersion = remoteConfig.getLong("latest_version")
        val lastOptionalVersion = remoteConfig.getLong("last_optional_version")
        val lastMandatoryVersion = remoteConfig.getLong("last_mandatory_version")
        Timber.e("$latestVersion $lastMandatoryVersion $lastOptionalVersion")
        when {
            currentVersion >= latestVersion -> {
                vm.hasConfigRetrieved = true
                Timber.e("currentVersion >= latestVersion")
            }
            lastMandatoryVersion > currentVersion -> {
                // Update App
                Timber.e("lastMandatoryVersion > currentVersion")
                MaterialDialog(this)
                    .show {
                        cancelable(false)
                        noAutoDismiss()
                        cancelOnTouchOutside(false)
                        title(R.string.app_update_dialog_title)
                        message(R.string.mandatory_update_message)
                        positiveButton(R.string.update_app) { updateApp() }
                        negativeButton(R.string.cancel) { killApp() }
                    }
            }
            lastOptionalVersion > currentVersion -> {
                // Tell user to update or skip
                Timber.e("lastOptionalVersion > currentVersion")
                MaterialDialog(this)
                    .show {
                        cancelable(true)
                        cancelOnTouchOutside(true)
                        title(R.string.app_update_dialog_title)
                        message(R.string.optional_update_message)
                        positiveButton(R.string.update_app) { updateApp() }
                        negativeButton(R.string.skip) {
                            vm.hasConfigRetrieved = true
                        }
                        onDismiss {
                            vm.hasConfigRetrieved = true
                        }
                    }
            }
            else -> {
                Timber.e("else")
                vm.hasConfigRetrieved = true
            }
        }
    }

    private fun updateApp() {
        startActivity(playStoreIntent())
    }

    private fun killApp() {
        finish()
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
        splashAnimator(binding.logo)
    }

    private fun splashAnimator(view: View) {
        view.scaleX = 1f
        view.scaleY = 1f
        view.alpha = 1f
        view.animate()
            .setInterpolator(LinearInterpolator())
            .alpha(0f)
//            .scaleX(3f)
//            .scaleY(3f)
            .setDuration(1000L)
            .withEndAction {
                if (!vm.hasConfigRetrieved) {
                    splashAnimator(view)
                } else {
                    vm.isViewAnimating = false
                }
            }
    }
}
