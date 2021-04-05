package `in`.nakkalites.mediaclient.view.profile

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivityProfileEditBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.login.CountriesBottomSheet
import `in`.nakkalites.mediaclient.view.login.CountriesBottomSheetCallbacks
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.viewmodel.profile.ProfileEditViewEvent
import `in`.nakkalites.mediaclient.viewmodel.profile.ProfileEditVm
import `in`.nakkalites.mediaclient.viewmodel.utils.NoUserFoundException
import `in`.nakkalites.mediaclient.viewmodel.utils.toCamelCase
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class ProfileEditActivity : BaseActivity(), CountriesBottomSheetCallbacks {
    private lateinit var binding: ActivityProfileEditBinding
    val vm: ProfileEditVm by viewModel()

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
                            finish()
                        }
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.profileLayout.visibility = View.VISIBLE
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
                val time = dob.split("/").map { it.toInt() }
                calendar.set(Calendar.YEAR, time[2])
                calendar.set(Calendar.MONTH, time[1])
                calendar.set(Calendar.DAY_OF_MONTH, time[0])
            }
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.select_date_of_birth))
                    .setSelection(calendar.timeInMillis)
                    .build()
            datePicker.addOnPositiveButtonClickListener {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it
                vm.dob.set(SimpleDateFormat("dd/MM/yyy", Locale.US).format(cal.time))
            }
            datePicker.show(supportFragmentManager, "tag")
        }

        override fun onFlagClicked() {
            val countries = vm.countryCodeVm
                .getCountriesList(resources.getStringArray(R.array.country_codes_data).asList())
            val sheet = CountriesBottomSheet.newInstance(countries)
            sheet.showAllowingStateLoss(supportFragmentManager)
        }

        override fun onCountryClicked() {
            val countriesList = resources.getStringArray(R.array.country_codes_data)
                .asList()
                .map { it.split(":")[2] }
                .toList()
            val countries = vm.countryCodeVm
                .getCountriesList(countriesList)
            val sheet = CountriesBottomSheet.newInstance(countries)
            sheet.showAllowingStateLoss(supportFragmentManager)
        }

        override fun onSaveClicked() {
            if (binding.etName.text.toString().trim().isEmpty()
                || binding.phoneEditText.text.toString().trim().isEmpty()
                || binding.etEmail.text.toString().trim().isEmpty()
            ) {
                Snackbar
                    .make(binding.root, getString(R.string.missing_fields), Snackbar.LENGTH_SHORT)
                    .show()
                return
            }
            vm.saveProfile()
        }
    }

    override fun onCountrySelected(position: Int) {
        vm.countryCodeVm.selectCountry(position)
    }
}

