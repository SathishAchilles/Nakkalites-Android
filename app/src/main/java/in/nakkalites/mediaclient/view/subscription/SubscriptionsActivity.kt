package `in`.nakkalites.mediaclient.view.subscription

import `in`.nakkalites.mediaclient.BR
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
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
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class SubscriptionsActivity : BaseActivity() , PaymentResultListener {
    private lateinit var binding: ActivitySubscriptionsBinding
    private val vm: SubscriptionsVm by viewModel()
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
                            goToCheckout(it.data.razorpayParams, it.data.apiKey)
                        }
                        is SubscriptionsEvent.TransactionStatus -> {
                            if (!it.data.status) {
                                Toast.makeText(this, it.data.error, Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(this, getString(R.string.payment_success_message), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
                is Result.Error -> {
                    errorHandler(it.throwable)
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.cta.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
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
        val co = Checkout()
        co.setKeyID(apiKey)
        try {
            val options = JSONObject(razorpayParams)
            co.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_SHORT)
                .show()
            e.printStackTrace()
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
        vm.items.filterIsInstance(SubscriptionVm::class.java)
            .forEach { it.isSelected.set(it.id == subscriptionVm.id) }
    }

    private val itemVmBinder: ViewModelBinder = viewModelBinder { itemBinding, itemVm ->
        itemBinding.setVariable(BR.vm, itemVm)
    }

    private val onCheckoutClick = {
        vm.getRazorPayParams()
    }

    /**
     * The name of the function has to be
     * onPaymentSuccess
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */
    override fun onPaymentSuccess(razorpayPaymentID: String) {
        try {
            vm.verifyPlan(razorpayPaymentID)
            Timber.e("Payment success: $razorpayPaymentID")
        } catch (e: java.lang.Exception) {
            Timber.e(e, "Exception in onPaymentSuccess")
        }
    }

    /**
     * The name of the function has to be
     * onPaymentError
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */
    override fun onPaymentError(code: Int, response: String) {
        try {
            Toast.makeText(this, "Payment failed: $code $response", Toast.LENGTH_SHORT).show()
            binding.recyclerView.visibility = View.VISIBLE
            binding.cta.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            Timber.e("Payment failed: $code $response")
        } catch (e: java.lang.Exception) {
            Timber.e(e, "Exception in onPaymentError")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Checkout.clearUserData(this)
    }
}
