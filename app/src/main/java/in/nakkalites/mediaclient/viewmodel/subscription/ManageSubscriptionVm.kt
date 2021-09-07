package `in`.nakkalites.mediaclient.viewmodel.subscription

import `in`.nakkalites.logging.logd
import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.domain.subscription.PlanManager
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class ManageSubscriptionVm(private val planManager: PlanManager) : BaseViewModel() {
    val benefits = ObservableArrayList<BenefitVm>()
    val planName = ObservableField<String>()
    val planImg = ObservableField<Int>()
    val planColorInt = ObservableInt()
    val planValidTill = ObservableField<String>()
    private val viewState = MutableLiveData<Event<Result<Unit>>>()

    fun viewStates(): LiveData<Event<Result<Unit>>> = viewState

    fun fetchPlans() {
        viewState.value = Event(Result.Loading())
        disposables += planManager.getPlans()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { planWrapper ->
                    logd(message = "Plans loaded")
                    planWrapper.plans
                        .firstOrNull { it.id != null && it.id == planWrapper.currentPlan?.id }
                        .let { plan ->
                            plan?.let {
                                planName.set(plan.name)
                                planImg.set(PlanUtils.getPlanIcon(plan))
                                planColorInt.set(PlanUtils.getPlanColorInt(plan.colorCode))
                                benefits.addAll(plan.descriptions.map {
                                    BenefitVm(ObservableBoolean(true), it)
                                })
                            }
                        }
                    planValidTill.set(planWrapper.currentPlan?.validTill)
                    viewState.value = Event(Result.Success(Unit))
                },
                onError = { throwable ->
                    loge(throwable = throwable, message = "Plans failed")
                    viewState.value = Event(Result.Error(Unit, throwable))
                })
    }

}
