package `in`.nakkalites.mediaclient.viewmodel.subscription

import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.StyleFormatText
import androidx.databinding.ObservableBoolean

class BenefitVm(val isSelected: ObservableBoolean, feature: String) : BaseViewModel(){
    val featureText = StyleFormatText(feature)
}
