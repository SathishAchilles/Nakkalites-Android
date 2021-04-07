package `in`.nakkalites.mediaclient.view.profile

import `in`.nakkalites.mediaclient.R
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
            handleLayoutVisibility(binding.nameLayout, user.name)
            handleLayoutVisibility(binding.emailLayout, user.email)
            handleLayoutVisibility(binding.phoneLayout, user.phoneNumber)
            handleLayoutVisibility(binding.genderLayout, user.gender)
            handleLayoutVisibility(binding.dobLayout, user.dob)
            handleLayoutVisibility(binding.cityLayout, user.city)
            handleLayoutVisibility(binding.countryLayout, user.country)
            setCurrentField()
            val view = when (profileAddVm.currentField) {
                ProfileFields.NAME -> binding.nameLayout
                ProfileFields.EMAIL -> binding.emailLayout
                ProfileFields.PHONE -> binding.phoneLayout
                ProfileFields.GENDER -> binding.genderLayout
                ProfileFields.DOB -> binding.dobLayout
                ProfileFields.COUNTRY -> binding.countryLayout
                ProfileFields.CITY -> binding.cityLayout
            }
            binding.viewAnimator.setDisplayedChild(view)
            view.visibility = View.VISIBLE
            profileAddVm.updateSkipVisibility()
            showHintIfPhoneLayout()
            showCTAText()
        }
        profileAddVm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> {
                    userManager.setAddProfileShown()
                    goToHome()
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
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

    private fun handleLayoutVisibility(view: View, value: String?) {
        if (!value.isNullOrEmpty()) {
            binding.viewAnimator.removeView(view)
        }
    }

    private fun showHintIfPhoneLayout() {
        if (profileAddVm.currentField == ProfileFields.PHONE) {
            requestHint()
        }
    }

    private fun showCTAText() {
        val child = binding.viewAnimator.getChildAt(binding.viewAnimator.childCount - 1)
        if (child.id != binding.viewAnimator.child.id) {
            profileAddVm.nextText.set(DisplayText.Singular(R.string.next))
        } else {
            profileAddVm.nextText.set(DisplayText.Singular(R.string.complete))
        }
    }

    private val callbacks = object : ProfileAddCallbacks {
        override fun onDateClicked() {
            val calendar = Calendar.getInstance()
            profileAddVm.dob.get()?.let { dob ->
                val time = dob.split("/").map { it.toInt() }
                calendar.set(Calendar.YEAR, time[2])
                calendar.set(Calendar.MONTH, time[1])
                calendar.set(Calendar.DAY_OF_MONTH, time[0])
            }
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.select_date_of_birth))
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
            datePicker.addOnPositiveButtonClickListener {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it
                profileAddVm.dob.set(SimpleDateFormat("dd/MM/yyy", Locale.US).format(cal.time))
            }
            datePicker.show(supportFragmentManager, "tag")
        }

        override fun onFlagClicked() {
            val countries = profileAddVm.countryCodeVm
                .getCountriesList(resources.getStringArray(R.array.country_codes_data).asList())
            val sheet = CountriesBottomSheet.newInstance(countries)
            sheet.showAllowingStateLoss(supportFragmentManager)
        }

        override fun onCountryClicked() {
            val countriesList = resources.getStringArray(R.array.country_codes_data)
                .asList()
                .map { it.split(":")[2] }
                .toList()
            val countries = profileAddVm.countryCodeVm
                .getCountriesList(countriesList)
            val sheet = CountriesBottomSheet.newInstance(countries)
            sheet.showAllowingStateLoss(supportFragmentManager)
        }

        override fun onSkipClicked() {
            onNextClicked()
        }

        override fun onNextClicked() {
            val child = binding.viewAnimator.getChildAt(binding.viewAnimator.childCount - 1)
            val emailText = binding.tvEmail.text.toString()
            if ((profileAddVm.currentField == ProfileFields.NAME && binding.tvName.text.toString()
                    .trim().isEmpty())
                || (profileAddVm.currentField == ProfileFields.PHONE && binding.phoneEditText.text.toString()
                    .trim().isEmpty())
                || (profileAddVm.currentField == ProfileFields.EMAIL && emailText.trim().isEmpty()
                        && validateEmail(emailText))
            ) {
                val errorMessage =
                    if ((profileAddVm.currentField == ProfileFields.EMAIL && validateEmail(emailText))) {
                        R.string.email_field_error
                    } else {
                        R.string.missing_fields
                    }
                Snackbar
                    .make(binding.root, getString(errorMessage), Snackbar.LENGTH_SHORT)
                    .show()
                return
            }
            if (child.id == binding.viewAnimator.child.id) {
                profileAddVm.saveProfile()
                profileAddVm.skipVisibility.set(false)
                return
            } else {
                binding.viewAnimator.showNext()
            }
            setCurrentField()
            profileAddVm.updateSkipVisibility()
            showHintIfPhoneLayout()
            showCTAText()
        }

        override fun onGenderClicked(type: GenderTypes) {
            profileAddVm.onGenderSelected(type)
        }
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
                profileAddVm.currentField = ProfileFields.PHONE
            }
            binding.genderLayout.id -> {
                profileAddVm.currentField = ProfileFields.GENDER
            }
            binding.dobLayout.id -> {
                profileAddVm.currentField = ProfileFields.DOB
            }
            binding.countryCode.id -> {
                profileAddVm.currentField = ProfileFields.COUNTRY
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

    override fun onCountrySelected(position: Int) {
        profileAddVm.countryCodeVm.selectCountry(position)
    }
}

enum class ProfileFields {
    NAME, EMAIL, PHONE, GENDER, DOB, COUNTRY, CITY
}

enum class GenderTypes {
    MALE, FEMALE, OTHERS
}
