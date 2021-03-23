package `in`.nakkalites.mediaclient.viewmodel.subscription

import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText

class ManageSubscriptionVm : BaseViewModel() {
    val isSelected = true
    val benefits = listOf<BaseViewModel>()
    val validTillDate = DisplayText.Plain("Valid till: 12 Feb 2021")
}
