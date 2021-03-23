package `in`.nakkalites.mediaclient.viewmodel.profile

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText

class ProfileEditVm : BaseViewModel() {
    val ageSuggestions = listOf<DisplayText>(
        DisplayText.Singular(R.string.male),
        DisplayText.Singular(R.string.female),
        DisplayText.Singular(R.string.others)
    )
}
