package `in`.nakkalites.mediaclient.viewmodel.subscription

import `in`.nakkalites.logging.logd
import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.models.Plan
import `in`.nakkalites.mediaclient.domain.subscription.PlanManager
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class SubscriptionsVm(
    private val planManager: PlanManager
) : BaseViewModel() {
    val items = ObservableArrayList<BaseModel>()
    var planUid: String? = null
    var name: String? = null
    var thumbnail = ObservableField<String?>()
    var selectedSubscription: Plan? = null
    private val viewState = MutableLiveData<Event<Result<SubscriptionsEvent>>>()
    val upgradablePlanCTA = ObservableField<DisplayText>()
    val isCTAEnabled = ObservableBoolean()
    var currentPlan: Plan? = null

    fun viewStates(): LiveData<Event<Result<SubscriptionsEvent>>> = viewState

    fun setArgs(name: String?, thumbnail: String?, planUid: String?) {
        this.planUid = planUid
        this.name = name
        this.thumbnail.set(thumbnail)
    }

    fun getPlans() {
        viewState.value = Event(Result.Loading(SubscriptionsEvent.PageLoading))
        disposables += planManager.getPlans()
            .observeOn(mainThread())
            .subscribeBy(
                onSuccess = { pair ->
                    logd(message = "Plans loaded")
                    items.addAll(pair.plans.map { plan ->
                        SubscriptionVm(plan, selectedSubscription?.id)
                    })
                    planUid?.let { id ->
                        items.filterIsInstance(SubscriptionVm::class.java)
                            .forEach { it.isSelected.set(it.id == id) }
                    }
                    val selected = items.filterIsInstance(SubscriptionVm::class.java)
                        .firstOrNull { it.isSelected.get() }
                    if (selected == null) {
                        pair.upgradeablePlan?.id?.let { id ->
                            items.filterIsInstance(SubscriptionVm::class.java)
                                .forEach { it.isSelected.set(it.id == id) }
                        }
                    }
                    selectedSubscription = items.filterIsInstance(SubscriptionVm::class.java)
                        .firstOrNull { it.isSelected.get() }?.plan
                    upgradablePlanCTA.set(
                        if (pair.currentPlan?.id != null) {
                            DisplayText.Singular(R.string.upgrade)
                        } else {
                            DisplayText.Singular(R.string.continue_to_payment)
                        }
                    )
                    if (thumbnail.get() == null) {
                        thumbnail.set(pair.planConfig?.thumbnail)
                    }
                    currentPlan = pair.currentPlan
                    isCTAEnabled.set(pair.currentPlan?.id == null || pair.currentPlan.id != selectedSubscription?.id)
                    viewState.value = Event(Result.Success(SubscriptionsEvent.PageLoaded))
                },
                onError = { throwable ->
                    loge(throwable = throwable, message = "Plans failed")
                    viewState.value =
                        Event(Result.Error(SubscriptionsEvent.PageLoadError, throwable))
                }
            )
    }

    fun getRazorPayParams() {
        viewState.value = Event(Result.Loading(SubscriptionsEvent.UpdateLoading))
        selectedSubscription?.id?.let { uid ->
            disposables += planManager.getRazorPayParams(uid)
                .observeOn(mainThread())
                .subscribeBy(
                    onSuccess = {
                        viewState.value =
                            Event(
                                Result.Success(
                                    SubscriptionsEvent.UpdateSuccess(
                                        it.second,
                                        it.first
                                    )
                                )
                            )
                    },
                    onError = { throwable ->
                        loge(throwable = throwable, message = "Plans failed")
                        viewState.value =
                            Event(Result.Error(SubscriptionsEvent.UpdateFailure, throwable))
                    }
                )
        }
    }

    fun verifyPlan(paymentId: String, orderId: String, signature: String) {
        viewState.value = Event(Result.Loading(SubscriptionsEvent.UpdateLoading))
        disposables += planManager.verifyPlan(paymentId, orderId, signature)
            .observeOn(mainThread())
            .subscribeBy(
                onSuccess = {
                    viewState.value =
                        Event(
                            Result.Success(
                                SubscriptionsEvent.TransactionStatus(it.first, it.second)
                            )
                        )
                },
                onError = { throwable ->
                    loge(throwable = throwable, message = "Plans failed")
                    viewState.value =
                        Event(Result.Error(SubscriptionsEvent.UpdateFailure, throwable))
                }
            )
    }

    fun subscriptionFailure(code: Int, message: String?, orderId: String) {
        viewState.value = Event(Result.Loading(SubscriptionsEvent.UpdateLoading))
        disposables += planManager.subscriptionFailure(orderId, code, message)
            .observeOn(mainThread())
            .subscribeBy(
                onComplete = {
                    viewState.value =
                        Event(Result.Success(SubscriptionsEvent.TransactionFailureStatus))
                },
                onError = { throwable ->
                    loge(throwable = throwable, message = "Plans failed")
                    viewState.value =
                        Event(Result.Error(SubscriptionsEvent.UpdateFailure, throwable))
                }
            )
    }

    fun onPlanSelected(subscriptionVm: SubscriptionVm) {
        isCTAEnabled.set(currentPlan?.id == null || currentPlan?.id != subscriptionVm.id)
    }
}


sealed class SubscriptionsEvent {
    object PageLoading : SubscriptionsEvent()
    object PageLoaded : SubscriptionsEvent()
    object PageLoadError : SubscriptionsEvent()
    object UpdateLoading : SubscriptionsEvent()
    data class UpdateSuccess(val apiKey: String?, val razorpayParams: Map<String, String>) :
        SubscriptionsEvent()

    data class TransactionStatus(val status: Boolean, val error: String?) : SubscriptionsEvent()
    object TransactionFailureStatus : SubscriptionsEvent()

    object UpdateFailure : SubscriptionsEvent()
}
