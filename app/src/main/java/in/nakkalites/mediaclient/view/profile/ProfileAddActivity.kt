package `in`.nakkalites.mediaclient.view.profile

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.ActivityProfileAddBinding
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.home.HomeActivity
import `in`.nakkalites.mediaclient.view.login.CountriesBottomSheet
import `in`.nakkalites.mediaclient.view.login.CountriesBottomSheetCallbacks
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.showSoftKeyboard
import `in`.nakkalites.mediaclient.view.utils.validateEmail
import `in`.nakkalites.mediaclient.viewmodel.profile.ProfileAddVm
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.utils.parsePhoneNumber
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.credentials.*
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ProfileAddActivity : BaseActivity(), CountriesBottomSheetCallbacks {
    private lateinit var binding: ActivityProfileAddBinding
    val profileAddVm: ProfileAddVm by viewModel()
    val userManager by inject<UserManager>()
    val phoneNumberUtil by inject<PhoneNumberUtil>()
    val analyticsManager by inject<AnalyticsManager>()
    private var totalCount = 0
    private var currentPagePos = 1

    companion object {
        private const val RESOLVE_HINT = 9002

        @JvmStatic
        fun createIntent(ctx: Context) =
            Intent(ctx, ProfileAddActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_add)
        binding.vm = profileAddVm
        binding.callbacks = callbacks
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        userManager.getUser()?.let { user ->
            val isNameAdded = handleLayoutVisibility(binding.nameLayout, user.name)
            val isEmailAdded = handleLayoutVisibility(binding.emailLayout, user.email)
            val isPhoneAdded = handleLayoutVisibility(binding.phoneLayout, user.phoneNumber)
            val isGenderAdded = handleLayoutVisibility(binding.genderLayout, user.gender)
            val isDobAdded = handleLayoutVisibility(binding.dobLayout, user.dob)
            val isCountryAdded = handleLayoutVisibility(binding.countryLayout, user.country)
            val isCityAdded = handleLayoutVisibility(binding.cityLayout, user.city)
            var view: View? = null
            when {
                isNameAdded -> {
                    profileAddVm.currentField = ProfileFields.NAME
                    view = binding.nameLayout
                }
                isEmailAdded -> {
                    profileAddVm.currentField = ProfileFields.EMAIL
                    view = binding.emailLayout
                }
                isPhoneAdded -> {
                    profileAddVm.currentField = ProfileFields.PHONE_NUMBER
                    view = binding.phoneLayout
                }
                isGenderAdded -> {
                    profileAddVm.currentField = ProfileFields.GENDER
                    view = binding.genderLayout
                }
                isDobAdded -> {
                    profileAddVm.currentField = ProfileFields.DOB
                    view = binding.dobLayout
                }
                isCountryAdded -> {
                    profileAddVm.currentField = ProfileFields.COUNTRY
                    profileAddVm.setDefaultCountry()
                    view = binding.countryLayout
                }
                isCityAdded -> {
                    profileAddVm.currentField = ProfileFields.CITY
                    view = binding.cityLayout
                }
            }
            if (isNameAdded) totalCount++
            if (isEmailAdded) totalCount++
            if (isPhoneAdded) totalCount++
            if (isGenderAdded) totalCount++
            if (isDobAdded) totalCount++
            if (isCountryAdded) totalCount++
            if (isCityAdded) totalCount++
            view?.let {
                binding.viewAnimator.setDisplayedChild(it)
                it.visibility = View.VISIBLE
            }
            if (totalCount != 1) {
                profileAddVm.updateSkipVisibility()
            } else {
                profileAddVm.skipVisibility.set(false)
            }
            showHintIfPhoneLayout()
            showCTAText()
        }
        profileAddVm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> {
                    userManager.setAddProfileShown()
                    trackProfileAddSuccess()
                    goToHome()
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    trackProfileAddFailure()
                    errorHandler(it.throwable)
                }
                else -> showLoading()
            }
        })
    }

    private fun goToHome() {
        HomeActivity.createIntent(this)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { startActivity(it) }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun handleLayoutVisibility(view: View, value: String?): Boolean {
        val remove = !value.isNullOrEmpty()
        if (remove) {
            binding.viewAnimator.removeView(view)
        }
        return !remove
    }

    private fun showHintIfPhoneLayout() {
        if (profileAddVm.currentField == ProfileFields.PHONE_NUMBER) {
            requestHint()
        }
    }

    private fun showCTAText() {
        if (totalCount == currentPagePos) {
            profileAddVm.nextText.set(DisplayText.Singular(R.string.complete))
        } else {
            profileAddVm.nextText.set(DisplayText.Singular(R.string.next))
        }
    }

    private val callbacks = object : ProfileAddCallbacks {
        override fun onDateClicked() {
            val calendar = Calendar.getInstance()
            val dob = profileAddVm.dob.get()
            val time = dob?.split("/")?.map {
                try {
                    it.toInt()
                } catch (e: NumberFormatException) {
                    null
                }
            }
            if (time?.filterNotNull()?.size == 3) {
                time[2]?.let { calendar.set(Calendar.YEAR, it) }
                time[1]?.let { calendar.set(Calendar.MONTH, it) }
                time[0]?.let { calendar.set(Calendar.DAY_OF_MONTH, it) }
            }
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.select_date_of_birth))
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
            datePicker.addOnPositiveButtonClickListener {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it
                profileAddVm.dob.set(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(cal.time))
            }
            datePicker.show(supportFragmentManager, "tag")
        }

        override fun onFlagClicked() {
            val countries = profileAddVm.countryCodeVm
                .getCountriesListForBottomSheet(resources.getStringArray(R.array.country_codes_data))
            val sheet = CountriesBottomSheet.newInstance(countries, true)
            sheet.showAllowingStateLoss(supportFragmentManager)
        }

        override fun onCountryClicked() {
            val countriesList = resources.getStringArray(R.array.country_data).asList()
            val sheet = CountriesBottomSheet.newInstance(countriesList, false)
            sheet.showAllowingStateLoss(supportFragmentManager)
        }

        override fun onSkipClicked() {
            onNextClicked()
        }

        override fun onNextClicked() {
            val emailText = binding.tvEmail.text.toString()
            if ((profileAddVm.currentField == ProfileFields.NAME
                        && binding.tvName.text.toString().trim().isEmpty())
                || (profileAddVm.currentField == ProfileFields.PHONE_NUMBER
                        && binding.phoneEditText.text.toString().trim().isEmpty())
                || (profileAddVm.currentField == ProfileFields.EMAIL && !validateEmail(emailText))
            ) {
                val errorMessage =
                    if ((profileAddVm.currentField == ProfileFields.EMAIL && !validateEmail(
                            emailText
                        ))
                    ) {
                        R.string.email_field_error
                    } else {
                        R.string.missing_fields
                    }
                Snackbar
                    .make(binding.root, getString(errorMessage), Snackbar.LENGTH_SHORT)
                    .show()
                return
            }
            if (currentPagePos == totalCount) {
                trackProfileAddSaveClicked()
                profileAddVm.saveProfile()
                profileAddVm.skipVisibility.set(false)
            } else {
                currentPagePos += 1
                binding.viewAnimator.showNext()
                setCurrentField()
                profileAddVm.updateSkipVisibility()
                showHintIfPhoneLayout()
                showCTAText()
            }
        }

        override fun onGenderClicked(type: GenderTypes) {
            profileAddVm.onGenderSelected(type)
        }
    }

    private fun trackProfileAddSaveClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.PROFILE_ADD_SAVE_CLICKED)
    }

    private fun trackProfileAddSuccess() {
        analyticsManager.logEvent(AnalyticsConstants.Event.PROFILE_ADD_SUCCESS)
    }

    private fun trackProfileAddFailure() {
        analyticsManager.logEvent(AnalyticsConstants.Event.PROFILE_ADD_FAILURE)
    }

    private fun setCurrentField() {
        when (binding.viewAnimator.currentView.id) {
            binding.nameLayout.id -> {
                profileAddVm.currentField = ProfileFields.NAME
            }
            binding.emailLayout.id -> {
                profileAddVm.currentField = ProfileFields.EMAIL
            }
            binding.phoneLayout.id -> {
                profileAddVm.currentField = ProfileFields.PHONE_NUMBER
            }
            binding.genderLayout.id -> {
                profileAddVm.currentField = ProfileFields.GENDER
            }
            binding.dobLayout.id -> {
                profileAddVm.currentField = ProfileFields.DOB
            }
            binding.countryLayout.id -> {
                profileAddVm.currentField = ProfileFields.COUNTRY
                profileAddVm.setDefaultCountry()
            }
            binding.cityLayout.id -> {
                profileAddVm.currentField = ProfileFields.CITY
            }
        }
    }

    private fun requestHint() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val options = CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build()
        val credentialsClient = Credentials.getClient(applicationContext, options)
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        try {
            startIntentSenderForResult(intent.intentSender, RESOLVE_HINT, null, 0, 0, 0, Bundle())
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                val credential = data?.getParcelableExtra(Credential.EXTRA_KEY) as? Credential
                val phoneNumber =
                    credential?.let { credential.id.parsePhoneNumber(phoneNumberUtil) }
                val nationalNumber = phoneNumber?.run { nationalNumber.toString() }
                if (nationalNumber != null) {
                    profileAddVm.onHintSelected(phoneNumber)
                    binding.phoneEditText.setText(nationalNumber)
                    binding.phoneEditText.setSelection(nationalNumber.length)
                }
            } else if (resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
                binding.phoneEditText.requestLayout()
                showSoftKeyboard(binding.phoneEditText)
            }
        }
    }

    override fun onCountrySelected(position: Int, withFlags: Boolean) {
        if (withFlags) {
            val countriesList = resources.getStringArray(R.array.country_codes_data).asList()
            profileAddVm.countryCodeVm.selectCountry(countriesList, position)
        } else {
            val countriesList = resources.getStringArray(R.array.country_data).asList()
            profileAddVm.selectCountry(countriesList, position)
        }
    }
}

enum class ProfileFields {
    NAME, EMAIL, PHONE_NUMBER, GENDER, DOB, COUNTRY, CITY
}

enum class GenderTypes {
    MALE, FEMALE, OTHERS
}
