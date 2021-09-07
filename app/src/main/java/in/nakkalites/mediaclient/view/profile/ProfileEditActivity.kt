package `in`.nakkalites.mediaclient.view.profile

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.ActivityProfileEditBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.login.CountriesBottomSheet
import `in`.nakkalites.mediaclient.view.login.CountriesBottomSheetCallbacks
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.validateEmail
import `in`.nakkalites.mediaclient.viewmodel.profile.ProfileEditViewEvent
import `in`.nakkalites.mediaclient.viewmodel.profile.ProfileEditVm
import `in`.nakkalites.mediaclient.viewmodel.utils.toCamelCase
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class ProfileEditActivity : BaseActivity(), CountriesBottomSheetCallbacks {
    private lateinit var binding: ActivityProfileEditBinding
    val vm: ProfileEditVm by viewModel()
    val analyticsManager by inject<AnalyticsManager>()

    companion object {

        @JvmStatic
        fun createIntent(ctx: Context) =
            Intent(ctx, ProfileEditActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_edit)
        binding.vm = vm
        binding.callbacks = callbacks
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        vm.fetchProfile()
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> {
                    when (it.data) {
                        is ProfileEditViewEvent.PageLoaded -> {
                            vm.user?.gender?.let { gender ->
                                binding.etGender.setText(gender.toCamelCase(), false)
                            }
                            binding.progressBar.visibility = View.GONE
                            binding.profileLayout.visibility = View.VISIBLE
                        }
                        is ProfileEditViewEvent.UpdateSuccess -> {
                            trackProfileEditSuccess()
                            finish()
                        }
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.profileLayout.visibility = View.VISIBLE
                    trackProfileEditFailure()
                    errorHandler(it.throwable)
                }
                else -> showLoading()
            }
        })
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private val callbacks = object : ProfileEditCallbacks {
        override fun onDateClicked() {
            val calendar = Calendar.getInstance()
            vm.dob.get()?.let { dob ->
                val time = dob.split("/").map {
                    try {
                        it.toInt()
                    } catch (e: NumberFormatException) {
                        null
                    }
                }
                if(time.filterNotNull().size == 3) {
                    time[2]?.let { calendar.set(Calendar.YEAR, it) }
                    time[1]?.let { calendar.set(Calendar.MONTH, it) }
                    time[0]?.let { calendar.set(Calendar.DAY_OF_MONTH, it) }
                }
            }
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.select_date_of_birth))
                    .setSelection(calendar.timeInMillis)
                    .build()
            datePicker.addOnPositiveButtonClickListener {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it
                vm.dob.set(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(cal.time))
            }
            datePicker.show(supportFragmentManager, "tag")
        }

        override fun onFlagClicked() {
            val countries = vm.countryCodeVm
                .getCountriesListForBottomSheet(resources.getStringArray(R.array.country_codes_data))
            val sheet = CountriesBottomSheet.newInstance(countries, true)
            sheet.showAllowingStateLoss(supportFragmentManager)
        }

        override fun onCountryClicked() {
            val countriesList = resources.getStringArray(R.array.country_data).asList()
            val sheet = CountriesBottomSheet.newInstance(countriesList, false)
            sheet.showAllowingStateLoss(supportFragmentManager)
        }

        override fun onSaveClicked() {
            if (binding.etName.text.toString().trim().isEmpty()
                || binding.phoneEditText.text.toString().trim().isEmpty()
                || (binding.etEmail.text.toString().trim()
                    .isEmpty() && validateEmail(binding.etEmail.text.toString()))
            ) {
                trackProfileEditSaveClicked()
                val errorMessage = if (validateEmail(binding.etEmail.text.toString())) {
                    R.string.email_field_error
                } else {
                    R.string.missing_fields
                }
                Snackbar
                    .make(binding.root, getString(errorMessage), Snackbar.LENGTH_SHORT)
                    .show()
                return
            }
            vm.saveProfile()
        }
    }

    override fun onCountrySelected(position: Int, withFlags: Boolean) {
        if (withFlags) {
            val countriesList = resources.getStringArray(R.array.country_codes_data).asList()
            vm.countryCodeVm.selectCountry(countriesList, position)
        } else {
            val countriesList = resources.getStringArray(R.array.country_data).asList()
            vm.selectCountry(countriesList, position)
        }
    }

    private fun trackProfileEditSaveClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.PROFILE_EDIT_SAVE_CLICKED)
    }

    private fun trackProfileEditSuccess() {
        analyticsManager.logEvent(AnalyticsConstants.Event.PROFILE_EDIT_SUCCESS)
    }

    private fun trackProfileEditFailure() {
        analyticsManager.logEvent(AnalyticsConstants.Event.PROFILE_EDIT_FAILURE)
    }
}

