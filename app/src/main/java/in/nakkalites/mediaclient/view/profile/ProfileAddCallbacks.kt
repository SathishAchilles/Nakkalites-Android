package `in`.nakkalites.mediaclient.view.profile

interface ProfileAddCallbacks {

    fun onDateClicked()

    fun onFlagClicked()

    fun onCountryClicked()

    fun onSkipClicked()

    fun onNextClicked()

    fun onGenderClicked(type: GenderTypes)
}
