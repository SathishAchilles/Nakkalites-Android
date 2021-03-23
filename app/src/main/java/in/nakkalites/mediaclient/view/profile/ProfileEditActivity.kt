package `in`.nakkalites.mediaclient.view.profile

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivityProfileEditBinding
import `in`.nakkalites.mediaclient.databinding.ActivitySplashBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.login.LoginActivity
import `in`.nakkalites.mediaclient.viewmodel.profile.ProfileEditVm
import `in`.nakkalites.mediaclient.viewmodel.splash.SplashVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileEditActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileEditBinding
    val vm: ProfileEditVm by viewModel()

    companion object {

        @JvmStatic
        fun createIntent(ctx: Context) =
            Intent(ctx, ProfileEditActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_edit)
        binding.vm = vm
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
    }
}
