package `in`.nakkalites.mediaclient.viewmodel.subscription

import `in`.nakkalites.mediaclient.domain.subscription.PlanManager
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.StyleFormatText
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class FaqListVm(private val planManager: PlanManager) : BaseViewModel() {
    val items = ObservableArrayList<BaseModel>()
    private val viewState = MutableLiveData<Event<Result<Unit>>>()

    fun viewStates(): LiveData<Event<Result<Unit>>> = viewState

    internal fun fetchFaqs() {
        disposables += planManager.getFaqs()
            .map {
                it.map { faq -> FaqVm(faq.question, faq.answer) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    viewState.value = Event(Result.Success(Unit))
                    items.addAll(it)
                },
                onError = {
                    viewState.value = Event(Result.Error(Unit, throwable = it))
                }
            )
    }
}

class FaqVm(question: String, answer: String) : BaseViewModel() {
    val question = StyleFormatText(question)
    val answer = StyleFormatText(answer)
    val showAnswer = ObservableBoolean(false)

    fun expand() {
        val shouldRotate = !showAnswer.get()
        showAnswer.set(shouldRotate)
    }
}
