package `in`.nakkalites.mediaclient.viewmodel.subscription

import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.databinding.ObservableBoolean

class SubscriptionVm(val planName:String) : BaseViewModel() {
    val isSelected = ObservableBoolean(false)
    val isMonthly = true
    val benefits = listOf<BaseViewModel>()
}
