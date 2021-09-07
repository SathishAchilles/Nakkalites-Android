package `in`.nakkalites.mediaclient.viewmodel.login

import `in`.nakkalites.mediaclient.domain.login.UserManager

object LoginUtils {

    fun shouldShowProfileAddPage(userManager: UserManager) =
        !userManager.isAddProfileShown() && !userManager.isProfileFieldsFilled()
}
