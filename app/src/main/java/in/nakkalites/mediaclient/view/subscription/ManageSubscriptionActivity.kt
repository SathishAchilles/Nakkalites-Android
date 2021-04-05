package `in`.nakkalites.mediaclient.view.subscription

import `in`.nakkalites.mediaclient.BR
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivityManageSubscriptionBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.ViewModelBinder
import `in`.nakkalites.mediaclient.view.binding.viewModelBinder
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.subscription.ManageSubscriptionVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.freshchat.consumer.sdk.Freshchat
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageSubscriptionActivity : BaseActivity() {
    private lateinit var binding: ActivityManageSubscriptionBinding
    private val vm: ManageSubscriptionVm by viewModel()

    companion object {
        @JvmStatic
        fun createIntent(ctx: Context): Intent = Intent(ctx, ManageSubscriptionActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_subscription)
        binding.vm = vm
        binding.vmBinder = itemVmBinder
        binding.onCTAClick = onCTAClick
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        vm.fetchPlans()
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> {
                    binding.mainLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
                is Result.Error -> {
                    errorHandler(it.throwable)
                }
                else -> showLoading()
            }
        })
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private val itemVmBinder: ViewModelBinder = viewModelBinder { itemBinding, itemVm ->
        itemBinding.setVariable(BR.vm, itemVm)
    }

    private val onCTAClick = {
        Freshchat.showConversations(this)
    }
}
