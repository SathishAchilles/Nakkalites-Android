package `in`.nakkalites.mediaclient.app.di

import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.domain.login.UserManager


class HeadersFactory(private val userManager: UserManager) {

    fun get(): Map<String, String> = mutableMapOf(
        Headers.APP_VERSION to BuildConfig.VERSION_NAME,
        Headers.APP_VERSION_CODE to BuildConfig.VERSION_CODE.toString()
    ).apply {
        userManager.getAccessToken()?.also { accessToken ->
            Headers.APP_ACCESS_TOKEN to accessToken
        }
        userManager.getUser()?.let { user -> Headers.APP_USER_ID to user.id }
    }
}

object Headers {
    const val APP_VERSION = "App-Version"
    const val APP_VERSION_CODE = "App-Version-Code"
    const val APP_USER_ID = "App-User-Id"
    const val APP_ACCESS_TOKEN = "Access-Token"
}
