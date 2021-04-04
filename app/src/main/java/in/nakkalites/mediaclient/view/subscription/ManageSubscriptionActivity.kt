package `in`.nakkalites.mediaclient.view.subscription

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivityManageSubscriptionBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.subscription.ManageSubscriptionVm
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar

class ManageSubscriptionActivity : BaseActivity() {
    private lateinit var binding: ActivityManageSubscriptionBinding
    private lateinit var vm: ManageSubscriptionVm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_subscription)
        binding.vm = vm
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        vm.fetchPlans()
        vm.viewStates().observe(this , EventObserver {
            when (it) {
                is Result.Success -> {
                    binding.mainLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
                is Result.Error -> {
                    Snackbar.make(
                        binding.root, getString(R.string.generic_error_message),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                else -> showLoading()
            }
        })
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

}
