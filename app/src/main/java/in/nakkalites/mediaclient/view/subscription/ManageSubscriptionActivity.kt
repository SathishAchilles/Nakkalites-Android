package `in`.nakkalites.mediaclient.view.subscription

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivityManageSubscriptionBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.viewmodel.subscription.ManageSubscriptionVm
import android.os.Bundle
import androidx.databinding.DataBindingUtil

class ManageSubscriptionActivity : BaseActivity() {
    private lateinit var binding: ActivityManageSubscriptionBinding
    private lateinit var vm: ManageSubscriptionVm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_subscription)
        binding.vm = vm
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
    }
}
