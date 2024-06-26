package `in`.nakkalites.mediaclient.app.di

import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.domain.login.UserDataStore


class HeadersFactory(private val userDataStore: UserDataStore) {

    fun get(): Map<String, String> {
        return mapOf(
            Headers.APP_VERSION to BuildConfig.VERSION_NAME,
            Headers.APP_VERSION_CODE to BuildConfig.VERSION_CODE.toString(),
            Headers.APP_ACCESS_TOKEN to userDataStore.getAccessToken(),
            Headers.APP_USER_ID to getUserId(),
            Headers.X_CLIENT_OS to "Android",
            Headers.X_CLIENT_ID to userDataStore.getInstanceIdOrEmpty()
        )
    }

    private fun getUserId() = userDataStore.getUser()?.id ?: ""
}

object Headers {
    const val APP_VERSION = "App-Version"
    const val APP_VERSION_CODE = "App-Version-Code"
    const val APP_USER_ID = "App-User-Id"
    const val APP_ACCESS_TOKEN = "Authorization"
    const val X_CLIENT_OS = "X-Client-OS"
    const val X_CLIENT_ID = "X-Client-ID"
}
