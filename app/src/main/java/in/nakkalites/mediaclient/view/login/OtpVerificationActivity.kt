package `in`.nakkalites.mediaclient.view.login

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.ActivityOtpVerificationBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.viewmodel.login.OtpVerificationVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class OtpVerificationActivity : BaseActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding
    private val otpVerificationVm: OtpVerificationVm by viewModel()

    companion object {

        @JvmStatic
        fun createIntent(ctx: Context, phoneNumber: String) =
            Intent(ctx, OtpVerificationActivity::class.java)
                .putExtra(AppConstants.PHONE_NUMBER, phoneNumber)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otp_verification)
        binding.vm = otpVerificationVm
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
    }
}
