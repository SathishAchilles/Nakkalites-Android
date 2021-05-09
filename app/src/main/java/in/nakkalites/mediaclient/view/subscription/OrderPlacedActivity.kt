package `in`.nakkalites.mediaclient.view.subscription

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.ActivityOrderPlacedBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.subscription.OrderPlacedVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrderPlacedActivity : BaseActivity() {
    private lateinit var binding: ActivityOrderPlacedBinding
    private val vm: OrderPlacedVm by viewModel()
    private val analyticsManager: AnalyticsManager by inject()

    private val planUid: String by lazy {
        intent.getStringExtra(AppConstants.PLAN_ID) ?: ""
    }
    private val planName: String by lazy {
        intent.getStringExtra(AppConstants.PLAN_NAME) ?: ""
    }
    private val razorpayPaymentID by lazy {
        intent.getStringExtra(AppConstants.RAZORPAY_ID) ?: ""
    }
    private val orderId by lazy {
        intent.getStringExtra(AppConstants.ORDER_ID) ?: ""
    }
    private val signature by lazy {
        intent.getStringExtra(AppConstants.SIGNATURE) ?: ""
    }

    companion object {
        @JvmStatic
        fun createIntent(
            ctx: Context,
            planUid: String?,
            planName: String?,
            razorpayPaymentID: String,
            orderId: String?,
            signature: String?
        ): Intent = Intent(ctx, OrderPlacedActivity::class.java)
            .putExtra(AppConstants.PLAN_ID, planUid)
            .putExtra(AppConstants.PLAN_NAME, planName)
            .putExtra(AppConstants.RAZORPAY_ID, razorpayPaymentID)
            .putExtra(AppConstants.ORDER_ID, orderId)
            .putExtra(AppConstants.SIGNATURE, signature)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_placed)
        binding.vm = vm
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        vm.verifyPlan(razorpayPaymentID, orderId, signature)
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> {
                    binding.mainLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    trackPlanUpdateSuccess(planName)
                }
                is Result.Error -> {
                    binding.mainLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    trackPlanUpdateFailure(planName, it.throwable.message)
                    errorHandler(it.throwable)
                }
                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun trackPlanUpdateSuccess(planName: String?) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.SELECTED_PLAN, planName)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.PLAN_UPDATE_SUCCESS, bundle)
    }

    private fun trackPlanUpdateFailure(planName: String?, error: String?) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.SELECTED_PLAN, planName)
            putString(AnalyticsConstants.Property.ERROR_MESSAGE, error)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.PLAN_UPDATE_FAILURE, bundle)
    }

}
