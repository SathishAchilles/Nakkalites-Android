package `in`.nakkalites.mediaclient.view.utils

import okhttp3.Request
import retrofit2.Retrofit

inline fun <reified T> Retrofit.create(): T {
    return create(T::class.java)
}

private val Request.apiRegex: Regex by lazy { Regex("^[\\w.-]+\\.meeshoapi\\.com\$") }
fun Request.isValidApiUrl(): Boolean {
    return apiRegex.matches(url.host)
}
