package `in`.nakkalites.mediaclient.viewmodel.login

import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.databinding.ObservableArrayList
import java.util.*

class CountriesSheetVm : BaseViewModel() {
    val countryVms = ObservableArrayList<CountryVm>()

    fun setArgs(countries: ArrayList<String>) {
        countryVms.apply {
            addAll(countries.mapIndexed { index, country -> CountryVm(country, index) })
        }
    }
}

class CountryVm(val country: String, val index: Int) : BaseViewModel()
