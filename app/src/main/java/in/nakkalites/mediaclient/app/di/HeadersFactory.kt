package `in`.nakkalites.mediaclient.app.di

import `in`.nakkalites.mediaclient.BuildConfig


class HeadersFactory {

    fun get(): Map<String, String> = mutableMapOf(
        Headers.APP_VERSION to BuildConfig.VERSION_NAME,
        Headers.APP_VERSION_CODE to BuildConfig.VERSION_CODE.toString()
    )
}

object Headers {
    const val APP_VERSION = "App-Version"
    const val APP_VERSION_CODE = "App-Version-Code"
}
