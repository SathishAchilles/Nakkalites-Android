package `in`.nakkalites.mediaclient.viewmodel.login

import `in`.nakkalites.mediaclient.app.constants.AppConstants.AppCountry
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.ObservableString


class CountryCodeVm(private val flagGlyphChecker: (String) -> Boolean) : BaseViewModel() {

    companion object {
        const val COUNTRY_DATA_DELIMITER = ':'

        fun formatFlagAndPhoneCode(flag: String?, phoneCodeWithPlus: String): String {
            val phoneCode = phoneCodeWithPlus.trim()
            return if (flag == null) "$phoneCode " else "$flag $phoneCode "
        }
    }

    private val countryData = mutableListOf<String>()
    private val countryDisplayText = mutableListOf<String>()
    private val countriesList = mutableListOf<String>()

    var phoneCode = AppCountry.DIALING_CODE

    val flagAndPhoneCode = run {
        val hasFlagGlyph = flagGlyphChecker.invoke(AppCountry.FLAG_EMOJI)
        val flag = if (hasFlagGlyph) AppCountry.FLAG_EMOJI else null
        ObservableString(formatFlagAndPhoneCode(flag, AppCountry.DIALING_CODE))
    }

    fun selectCountry(countriesList: List<String>, position: Int) {
        val (phoneCode, isoCode, _) = countriesList[position].split(COUNTRY_DATA_DELIMITER)
        this.phoneCode = "+$phoneCode"
        setFlagAndPhoneCode(isoCode, phoneCode)
    }

    private fun getCountriesList(entries: Array<String>): List<String> {
        if (countryDisplayText.isNotEmpty()) return countryDisplayText

        entries.forEach { entry ->
            countryData.add(entry)
            val (phoneCode, isoCode, country) = entry.split(COUNTRY_DATA_DELIMITER)
            val emoji = isoCodeToEmojiFlag(isoCode)
            val displayText = buildString {
                if (emoji != null) {
                    append("$emoji     ")
                }
                append("$country (+$phoneCode)")
            }
            countryDisplayText.add(displayText)
        }
        return countryDisplayText
    }

    fun setFlagAndPhoneCode(isoCode: String, phoneCode: String) {
        flagAndPhoneCode.set(formatFlagAndPhoneCode(isoCodeToEmojiFlag(isoCode), phoneCode))
    }

    private fun isoCodeToEmojiFlag(isoCode: String): String? {
        val flagOffset = 127462
        val asciiOffset = 65
        val firstChar = Character.codePointAt(isoCode, 0) - asciiOffset + flagOffset
        val secondChar = Character.codePointAt(isoCode, 1) - asciiOffset + flagOffset
        val emoji = String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        val canShowFlagEmoji = flagGlyphChecker.invoke(emoji)
        return if (canShowFlagEmoji) emoji else null
    }

    fun getCountriesListForBottomSheet(entries: Array<String>) :List<String>{
        if (countriesList.isNotEmpty()) {
            return countriesList
        }
        getCountriesList(entries).map { displayText ->
            countriesList.add(displayText)
        }
        return countriesList
    }
}
