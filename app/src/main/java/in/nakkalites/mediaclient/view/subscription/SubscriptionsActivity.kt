package `in`.nakkalites.mediaclient.view.subscription

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.ActivitySubscriptionsBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.video.VideoPlayerActivity
import `in`.nakkalites.mediaclient.viewmodel.subscription.SubscriptionsVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil

class SubscriptionsActivity : BaseActivity() {
    private lateinit var binding: ActivitySubscriptionsBinding
    private lateinit var vm: SubscriptionsVm
    companion object {
        @JvmStatic
        fun createIntent(ctx: Context): Intent = Intent(ctx, SubscriptionsActivity::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscriptions)
        binding.vm = vm
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)

    }
}
