package `in`.nakkalites.mediaclient.viewmodel.profile

import `in`.nakkalites.logging.logd
import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.view.profile.GenderTypes
import `in`.nakkalites.mediaclient.view.profile.ProfileFields
import `in`.nakkalites.mediaclient.view.utils.Event
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.getIsoCode
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.login.CountryCodeVm
import `in`.nakkalites.mediaclient.viewmodel.login.glyphChecker
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.utils.UserUpdateFailedException
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import java.util.*

class ProfileAddVm(
    private val phoneNumberUtil: PhoneNumberUtil, private val userManager: UserManager
) : BaseViewModel() {

    val name = ObservableField<String>()
    val email = ObservableField<String>()
    val phoneNumber = ObservableField<String>()
    val dob = ObservableField<String>()
    val gender = ObservableField<String>()
    val country = ObservableField<String>(AppConstants.AppCountry.NAME)
    val city = ObservableField<String>()
    var currentField = ProfileFields.NAME
    val skipVisibility = ObservableBoolean(false)
    val nextText = ObservableField<DisplayText>(DisplayText.Singular(R.string.next))
    val countryCodeVm = CountryCodeVm(flagGlyphChecker = glyphChecker)
    private var phoneNumberValue: Phonenumber.PhoneNumber? = null
    private val viewState = MutableLiveData<Event<Result<Unit>>>()

    fun viewStates(): LiveData<Event<Result<Unit>>> = viewState

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
                onComplete = { Result.Success(Unit) },
                onError = { throwable ->
                    Result.Error(Unit, UserUpdateFailedException())
                    loge(throwable = throwable, message = "Profile update failed")
                })
    }

    fun updateSkipVisibility() {
        val mandatoryList =
            listOf<ProfileFields>(ProfileFields.NAME, ProfileFields.PHONE, ProfileFields.EMAIL)
        skipVisibility.set(!mandatoryList.contains(currentField))
    }


    fun onHintSelected(phoneNumber: Phonenumber.PhoneNumber) {
        this.phoneNumberValue = phoneNumber
        this.countryCodeVm.phoneCode = "+${phoneNumber.countryCode}"
        val isoCode = phoneNumber.getIsoCode(phoneNumberUtil)
        countryCodeVm.setFlagAndPhoneCode(isoCode, countryCodeVm.phoneCode)
    }

    fun onGenderSelected(type: GenderTypes) {
        gender.set(type.name.toLowerCase(Locale.US))
    }
}
