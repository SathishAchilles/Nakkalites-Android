package `in`.nakkalites.mediaclient.viewmodel.home

enum class HomeTab(val position: Int) {
    ALL(0), WEB_SERIES(1);

    companion object {
        fun fromPosition(position: Int) = values().single { it.position == position }
    }
}
