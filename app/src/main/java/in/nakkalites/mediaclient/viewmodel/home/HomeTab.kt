package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.R

enum class HomeTab(val position: Int, val stringRes: Int) {
    ALL(0, R.string.all),
    WEB_SERIES(1, R.string.web_series);

    companion object {
        fun fromPosition(position: Int) = values().single { it.position == position }
    }
}
