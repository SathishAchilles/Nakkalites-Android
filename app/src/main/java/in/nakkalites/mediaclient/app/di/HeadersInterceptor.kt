package `in`.nakkalites.mediaclient.app.di

import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.view.utils.isValidApiUrl
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class HeadersInterceptor(private val userManager: UserManager) : Interceptor {
    private val maxRetries = 3

    override fun intercept(chain: Interceptor.Chain): Response {
        if (proceedToNextInterceptor(chain)) return chain.proceed(chain.request())

        val origRequest = chain.request()
        var response = chain.proceed(rewriteHeaders(origRequest))
        var count = 1
        while (count < maxRetries) {
            response = chain.proceed(rewriteHeaders(origRequest))
            count++
        }
        return response
    }

    private fun proceedToNextInterceptor(chain: Interceptor.Chain): Boolean {
        val request = chain.request()
        return !request.isValidApiUrl()
    }

    private fun rewriteHeaders(request: Request): Request = request.newBuilder()
        .headers(HeadersFactory(userManager).get().toHeaders())
        .build()
}
