package `in`.nakkalites.mediaclient.viewmodel.subscription

import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.databinding.ObservableArrayList

class SubscriptionsVm : BaseViewModel() {
    val thumbnail = ""
    val subscriptions = ObservableArrayList<BaseViewModel>()
}
