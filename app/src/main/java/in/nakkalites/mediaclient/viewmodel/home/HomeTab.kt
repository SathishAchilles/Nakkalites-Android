package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.R
import android.view.MenuItem
import androidx.annotation.IdRes

enum class HomeTab(val position: Int, val stringRes: Int, @IdRes val menuId: Int) {
    ALL(0, R.string.home, R.id.navigation_home),
    WEB_SERIES(1, R.string.web_series, R.id.navigation_web_series),
    PROFILE(2, R.string.profile, R.id.navigation_profile);

    companion object {

        fun create(menuItem: MenuItem?): HomeTab =
            values().single { it.menuId == menuItem?.itemId }

    }
}
