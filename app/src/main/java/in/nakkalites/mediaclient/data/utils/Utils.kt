package `in`.nakkalites.mediaclient.data.utils

import android.content.SharedPreferences

// Workaround for https://github.com/square/retrofit/issues/1805 issue.
typealias StringAnyMap = Map<String, @JvmSuppressWildcards Any>

fun SharedPreferences.getStringOrEmpty(key: String): String = getString(key, "")!!
