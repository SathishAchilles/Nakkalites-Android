package `in`.nakkalites.mediaclient.view.home

import `in`.nakkalites.logging.logd
import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.domain.subscription.PlanManager
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.subscription.PlanUtils
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.utils.ObservableString
import `in`.nakkalites.mediaclient.viewmodel.utils.StyleFormatText
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class UserProfileVm(val userManager: UserManager, planManager: PlanManager) : BaseViewModel() {

    private var user = userManager.getUser()
    val profileUrl = ObservableField<String>(user?.imageUrl)
    val name = ObservableString(user?.let {
        it.name ?: it.phoneNumber ?: it.email ?: ""
    } ?: "")
    val planImg = ObservableField(PlanUtils.getPlanIcon(user?.plan))
    val planName = ObservableField(user?.plan?.name)
    val planColorInt = ObservableInt(PlanUtils.getPlanColorInt(userManager.getCurrentPlansColor()))
    val upgradableMessage = ObservableField<StyleFormatText>()
    val upgradableViewVisibility = ObservableBoolean()
    val manageSubscriptionsVisibility = ObservableBoolean()
    val upgradablePrice = ObservableField<DisplayText>()
    val upgradablePlanFrequency = ObservableField<DisplayText>()
    val upgradablePlanCTA = ObservableField<DisplayText>()
    val version = "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    private val viewState = MutableLiveData<Event<Result<Unit>>>()

    init {
        disposables += planManager.getPlanObserver()
            .filter { it }
            .observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                fetchProfile()
                logd("Refresh Page")
            })
    }

    fun viewStates(): LiveData<Event<Result<Unit>>> = viewState

    fun fetchProfile() {
        viewState.value = Event(Result.Loading())
        disposables += userManager.getUserProfile()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    user = it
                    profileUrl.set(it.imageUrl)
                    planName.set(it.plan?.name)
                    planImg.set(PlanUtils.getPlanIcon(it?.plan))
                    planColorInt.set(PlanUtils.getPlanColorInt(userManager.getCurrentPlansColor()))
                    it.upgradablePlan?.let { plan ->
                        plan.promotionText?.let { upgradableMessage.set(StyleFormatText(plan.promotionText)) }
                        plan.price?.let {
                            upgradablePrice.set(
                                DisplayText.Singular(R.string.rupee_x, listOf(plan.price))
                            )
                        }
                        plan.frequency?.let {
                            upgradablePlanFrequency.set(
                                DisplayText.Singular(R.string.slash_x, listOf(plan.frequency))
                            )
                        }
                        upgradablePlanCTA.set(
                            if (plan.availablePlansCount != null && plan.availablePlansCount <= 1
                                && plan.name != null
                            ) {
                                DisplayText.Singular(R.string.upgrade_to_x, listOf(plan.name))
                            } else {
                                DisplayText.Singular(R.string.view_plans)
                            }
                        )
                    }
                    upgradableViewVisibility.set(it.upgradablePlan != null)
                    manageSubscriptionsVisibility.set(user?.plan != null)
                    viewState.value = Event(Result.Success(Unit))
                },
                onError = { throwable ->
                    viewState.value = Event(Result.Error(Unit, throwable))
                    loge(throwable = throwable, message = "Profile update failed")
                })
    }
}
