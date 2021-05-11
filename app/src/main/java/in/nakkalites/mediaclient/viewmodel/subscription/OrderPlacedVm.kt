package `in`.nakkalites.mediaclient.viewmodel.subscription

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.domain.subscription.PlanManager
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class OrderPlacedVm(private val planManager: PlanManager) : BaseViewModel() {

    val isSuccessful = ObservableBoolean()

    private val viewState = MutableLiveData<Event<Result<Unit>>>()

    fun viewStates(): LiveData<Event<Result<Unit>>> = viewState

    fun verifyPlan(paymentId: String, orderId: String, signature: String, membershipId: String) {
        viewState.value = Event(Result.Loading())
        disposables += planManager.verifyPlan(paymentId, orderId, signature, membershipId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    isSuccessful.set(it.first)
                    viewState.value = Event(Result.Success(Unit))
                },
                onError = { throwable ->
                    loge(throwable = throwable, message = "Plans failed")
                    viewState.value = Event(Result.Error(Unit, throwable = throwable))
                }
            )
    }

}
