package `in`.nakkalites.mediaclient.viewmodel.profile

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.login.CountryCodeVm
import `in`.nakkalites.mediaclient.viewmodel.login.glyphChecker
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.utils.UserUpdateFailedException
import `in`.nakkalites.mediaclient.viewmodel.utils.toCamelCase
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class ProfileEditVm(val userManager: UserManager) : BaseViewModel() {
    val ageSuggestions = listOf<DisplayText>(
        DisplayText.Singular(R.string.male),
        DisplayText.Singular(R.string.female),
        DisplayText.Singular(R.string.others)
    )
    val name = ObservableField<String>()
    val email = ObservableField<String>()
    val phoneNumber = ObservableField<String>()
    val dob = ObservableField<String>()
    val gender = ObservableField<String>()
    val country = ObservableField<String>(AppConstants.AppCountry.NAME)
    val city = ObservableField<String>()
    val phoneNumberEnabled = ObservableBoolean()
    val emailEnabled = ObservableBoolean()
    val countryCodeVm = CountryCodeVm(flagGlyphChecker = glyphChecker)
    var user = userManager.getUser()

    private val viewState = MutableLiveData<Event<Result<ProfileEditViewEvent>>>()

    fun viewStates(): LiveData<Event<Result<ProfileEditViewEvent>>> = viewState

    fun saveProfile() {
        viewState.value = Event(Result.Loading())
        disposables += userManager.updateUserProfile(
            name.get(),
            countryCodeVm.phoneCode,
            phoneNumber.get(),
            email.get(),
            gender.get(),
            dob.get(),
            country.get(),
            city.get()
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    viewState.value = Event(Result.Success(ProfileEditViewEvent.UpdateSuccess))
                },
                onError = { throwable ->
                    viewState.value = Event(
                        Result.Error(
                            ProfileEditViewEvent.UpdateFailure,
                            throwable
                        )
                    )
                    loge(throwable = throwable, message = "Profile update failed")
                })
    }

    fun fetchProfile() {
        viewState.value = Event(Result.Loading())
        disposables += userManager.getUserProfile()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    user = it
                    name.set(it.name)
                    email.set(it.email)
                    gender.set(it.gender?.toCamelCase())
                    dob.set(it.dob)
                    country.set(it.country)
                    city.set(it.city)
                    phoneNumber.set(it.phoneNumber)
                    phoneNumberEnabled.set(it.phoneNumber != null)
                    emailEnabled.set(it.email != null)
                    it.countryCode?.let { countryCode -> countryCodeVm.phoneCode = countryCode }
                    viewState.value = Event(Result.Success(ProfileEditViewEvent.PageLoaded))
                },
                onError = { throwable ->
                    viewState.value = Event(Result.Success(ProfileEditViewEvent.PageLoadError))
                    loge(throwable = throwable, message = "Profile save failed")
                })
    }
}

sealed class ProfileEditViewEvent {
    object PageLoaded : ProfileEditViewEvent()
    object PageLoadError : ProfileEditViewEvent()
    object UpdateSuccess : ProfileEditViewEvent()
    object UpdateFailure : ProfileEditViewEvent()
}
