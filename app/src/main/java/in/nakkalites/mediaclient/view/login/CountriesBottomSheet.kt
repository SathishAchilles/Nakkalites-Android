package `in`.nakkalites.mediaclient.view.login

import `in`.nakkalites.mediaclient.BR
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.FragmentCountriesBottomSheetBinding
import `in`.nakkalites.mediaclient.view.BaseBottomSheetFragment
import `in`.nakkalites.mediaclient.view.binding.RecyclerViewAdapter
import `in`.nakkalites.mediaclient.view.binding.viewModelBinder
import `in`.nakkalites.mediaclient.view.binding.viewProvider
import `in`.nakkalites.mediaclient.view.utils.commitAllowingStateLoss
import `in`.nakkalites.mediaclient.viewmodel.login.CountriesSheetVm
import `in`.nakkalites.mediaclient.viewmodel.login.CountryVm
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class CountriesBottomSheet : BaseBottomSheetFragment() {
    private var callback: CountriesBottomSheetCallbacks? = null
    private val vm: CountriesSheetVm by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCountriesBottomSheetBinding.inflate(LayoutInflater.from(context))
        val countries = requireArguments().getStringArrayList(COUNTRY_LIST)!!
        vm.setArgs(countries)
        val adapter =
            RecyclerViewAdapter(vm.countryVms, viewProvider { R.layout.item_country }, viewBinder)
        binding.recyclerView.adapter = adapter
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as CountriesBottomSheetCallbacks
        } catch (e: ClassCastException) {
            error("$context should implement CountriesBottomSheetCallbacks")
        }
    }

    private val viewBinder = viewModelBinder { entryBinding, _ ->
        entryBinding.setVariable(BR.onCountrySelected, onCountrySelected)
    }

    private val onCountrySelected = { vm: CountryVm ->
        callback?.onCountrySelected(vm.index)
        dismissAllowingStateLoss()
    }

    fun showAllowingStateLoss(fm: FragmentManager) {
        commitAllowingStateLoss(this, fm, "countries-selection-sheet")
    }

    companion object {
        private const val COUNTRY_LIST = "COUNTRY_LIST"

        fun newInstance(countries: List<String>): CountriesBottomSheet {
            val args = Bundle()
            args.putStringArrayList(COUNTRY_LIST, countries as ArrayList<String>)
            return CountriesBottomSheet().apply { arguments = args }
        }
    }
}

interface CountriesBottomSheetCallbacks {
    fun onCountrySelected(position: Int)
}
