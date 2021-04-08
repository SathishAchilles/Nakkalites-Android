package `in`.nakkalites.mediaclient.view.subscription

import `in`.nakkalites.mediaclient.BR
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.ActivitySubscriptionsBinding
import `in`.nakkalites.mediaclient.databinding.ItemSubscriptionBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.*
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.argumentError
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.subscription.SubscriptionVm
import `in`.nakkalites.mediaclient.viewmodel.subscription.SubscriptionsEvent
import `in`.nakkalites.mediaclient.viewmodel.subscription.SubscriptionsVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class SubscriptionsActivity : BaseActivity(), PaymentResultWithDataListener {
    private lateinit var binding: ActivitySubscriptionsBinding
    private val vm: SubscriptionsVm by viewModel()
    private val analyticsManager: AnalyticsManager by inject()
    private val planUid: String? by lazy {
        intent.getStringExtra(AppConstants.PLAN_ID)
    }
    private val name by lazy {
        intent.getStringExtra(AppConstants.VIDEO_NAME)
    }
    private val thumbnail by lazy {
        intent.getStringExtra(AppConstants.VIDEO_THUMBNAIL)
    }

    companion object {
        @JvmStatic
        fun createIntent(
            ctx: Context,
            name: String? = null,
            thumbnail: String? = null,
            planUid: String? = null
        ): Intent = Intent(ctx, SubscriptionsActivity::class.java)
            .putExtra(AppConstants.PLAN_ID, planUid)
            .putExtra(AppConstants.VIDEO_NAME, name)
            .putExtra(AppConstants.VIDEO_THUMBNAIL, thumbnail)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscriptions)
        binding.vm = vm
        binding.onCTAClicked = onCheckoutClick
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        Checkout.preload(applicationContext)
        vm.setArgs(name, thumbnail, planUid)
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> {
                    when (it.data) {
                        is SubscriptionsEvent.PageLoaded -> {
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.cta.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE
                        }
                        is SubscriptionsEvent.UpdateSuccess -> {
                            trackPlanOrderFetchSuccess(
                                vm.selectedSubscription?.name,
                                it.data.razorpayParams
                            )
                            goToCheckout(it.data.razorpayParams, it.data.apiKey)
                        }
                        SubscriptionsEvent.TransactionFailureStatus -> {
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.cta.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE
                        }
                        is SubscriptionsEvent.TransactionStatus -> {
                            if (!it.data.status) {
                                trackPlanUpdateFailure(vm.selectedSubscription?.name, it.data.error)
                                Toast.makeText(this, it.data.error, Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                trackPlanUpdateSuccess(vm.selectedSubscription?.name)
                                Toast.makeText(
                                    this,
                                    getString(R.string.payment_success_message),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                }
                is Result.Error -> {
                    if (it.initialValue == SubscriptionsEvent.UpdateFailure) {
                        trackPlanOrderFetchFailure(vm.selectedSubscription?.name)
                    }
                    errorHandler(it.throwable)
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.cta.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
                is Result.Loading -> {
                    when (it.initialValue) {
                        SubscriptionsEvent.UpdateLoading -> {
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.cta.visibility = View.GONE
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        else -> showLoading()
                    }
                }
                else -> showLoading()
            }
        })
        init()
    }

    private fun showLoading() {
        binding.recyclerView.visibility = View.GONE
        binding.cta.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun goToCheckout(razorpayParams: Map<String, String>, apiKey: String?) {
        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */
        val checkout = Checkout()
        checkout.setKeyID(apiKey)
        try {
            val options = JSONObject(razorpayParams)
            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_SHORT)
                .show()
            Timber.e(e)
        }
    }

    private fun init() {
        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this)
        val viewAdapter = RecyclerViewAdapter<BaseModel>(vm.items, viewProvider, vmBinder)
        recyclerView.adapter = viewAdapter
        recyclerView.layoutManager = layoutManager
        vm.getPlans()
    }

    private val viewProvider = ViewProviders.wrapSequentially(
        viewProvider { vm1: BaseModel ->
            when (vm1) {
                is SubscriptionVm -> R.layout.item_subscription
                else -> argumentError()
            }
        })

    private val vmBinder = viewModelBinder { itemBinding, vm1 ->
        when (vm1) {
            is SubscriptionVm -> {
                val binding = (itemBinding as ItemSubscriptionBinding)
                binding.vm = vm1
                binding.onSelected = onSelected
                binding.vmBinder = itemVmBinder
            }
        }
    }
    private val onSelected = { subscriptionVm: SubscriptionVm ->
        vm.selectedSubscription = subscriptionVm.plan
        vm.onPlanSelected(subscriptionVm)
        vm.items.filterIsInstance(SubscriptionVm::class.java)
            .forEach { it.isSelected.set(it.id == subscriptionVm.id) }
    }

    private val itemVmBinder: ViewModelBinder = viewModelBinder { itemBinding, itemVm ->
        itemBinding.setVariable(BR.vm, itemVm)
    }

    private val onCheckoutClick = {
        trackPlanSelectedClicked(vm.selectedSubscription?.name)
        vm.getRazorPayParams()
    }


    override fun onPaymentSuccess(razorpayPaymentID: String, paymentData: PaymentData) {
        try {
            trackRazorpaySuccess(vm.selectedSubscription?.name)
            vm.verifyPlan(razorpayPaymentID, paymentData.orderId, paymentData.signature)
        } catch (e: java.lang.Exception) {
            Timber.e(e, "Exception in onPaymentSuccess")
        }
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData) {
        try {
            trackRazorpayFailure(vm.selectedSubscription?.name)
            vm.subscriptionFailure(code, response, paymentData.orderId)
            Toast.makeText(this, "Payment failed: $code $response", Toast.LENGTH_SHORT).show()
            Timber.e("Payment failed: $code $response")
        } catch (e: java.lang.Exception) {
            Timber.e(e, "Exception in onPaymentError")
        }
    }

    private fun trackPlanSelectedClicked(planName: String?) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.SELECTED_PLAN, planName)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.PLAN_SELECTED, bundle)
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

    private fun trackPlanOrderFetchSuccess(planName: String?, razorpayParams: Map<String, String>) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.SELECTED_PLAN, planName)
            razorpayParams.map {
                putString(it.key, it.value)
            }
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.PLAN_ORDER_FETCH_SUCCESS, bundle)
    }

    private fun trackPlanOrderFetchFailure(planName: String?) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.SELECTED_PLAN, planName)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.PLAN_ORDER_FETCH_FAILURE, bundle)
    }

    private fun trackRazorpaySuccess(planName: String?) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.SELECTED_PLAN, planName)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.RAZORPAY_SUCCESS, bundle)
    }

    private fun trackRazorpayFailure(planName: String?) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.SELECTED_PLAN, planName)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.RAZORPAY_FAILURE, bundle)
    }

    override fun onDestroy() {
        super.onDestroy()
        Checkout.clearUserData(this)
    }
}
