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
    var thumbnail: String? = null
    var selectedSubscription: Plan? = null
    private val viewState = MutableLiveData<Event<Result<SubscriptionsEvent>>>()
    val upgradablePlanCTA = ObservableField<DisplayText>()

    fun viewStates(): LiveData<Event<Result<SubscriptionsEvent>>> = viewState

    fun setArgs(name: String?, thumbnail: String?, planUid: String?) {
        this.planUid = planUid
        this.name = name
        this.thumbnail = thumbnail
            ?: "https://d3ne42l1zea0it.cloudfront.net/variants/hvyg29gp02ymh8heqc543n9m6z0s/bbb2200f8e3e886ec6529be6066fc9af77251d291157e2a3933f3e08aab54c02"
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
                    val isPlanSelected = items.filterIsInstance(SubscriptionVm::class.java)
                        .firstOrNull { it.isSelected.get() }
                    if (isPlanSelected == null) {
                        pair.upgradeablePlan?.id?.let { id ->
                            items.filterIsInstance(SubscriptionVm::class.java)
                                .forEach { it.isSelected.set(it.id == id) }
                        }
                    }
                    selectedSubscription = items.filterIsInstance(SubscriptionVm::class.java)
                        .firstOrNull { it.isSelected.get() }?.plan
                    val upgradablePlanName =
                        pair.plans.find { it.id == pair.upgradeablePlan?.id }?.name
                    upgradablePlanCTA.set(
                        if (pair.currentPlan?.id != null) {
                            DisplayText.Singular(R.string.upgrade)
                        } else {
                            DisplayText.Singular(R.string.continue_to_payment)
                        }
                    )
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

    fun verifyPlan(paymentId: String) {
        viewState.value = Event(Result.Loading(SubscriptionsEvent.UpdateLoading))
        disposables += planManager.verifyPlan(paymentId)
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
}


sealed class SubscriptionsEvent {
    object PageLoading : SubscriptionsEvent()
    object PageLoaded : SubscriptionsEvent()
    object PageLoadError : SubscriptionsEvent()
    object UpdateLoading : SubscriptionsEvent()
    data class UpdateSuccess(val apiKey: String?, val razorpayParams: Map<String, String>) :
        SubscriptionsEvent()

    data class TransactionStatus(val status: Boolean, val error: String?) : SubscriptionsEvent()

    object UpdateFailure : SubscriptionsEvent()
}
