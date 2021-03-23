package `in`.nakkalites.mediaclient.view.profile

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.ActivityOtpVerificationBinding
import `in`.nakkalites.mediaclient.databinding.ActivityProfileAddBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.login.OtpVerificationActivity
import `in`.nakkalites.mediaclient.viewmodel.login.OtpVerificationVm
import `in`.nakkalites.mediaclient.viewmodel.profile.ProfileAddVm
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileAddActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileAddBinding
    private val profileAddVm: ProfileAddVm by viewModel()

    companion object {

        @JvmStatic
        fun createIntent(ctx: Context) =
            Intent(ctx, ProfileAddActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_add)
        binding.vm = profileAddVm
        binding.callbacks = callbacks
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
    }

    private val callbacks = object : ProfileAddCallbacks {
        override fun onSkipClicked() {

        }

        override fun onNextClicked() {
            binding.viewAnimator.showNext()
        }
    }
}
