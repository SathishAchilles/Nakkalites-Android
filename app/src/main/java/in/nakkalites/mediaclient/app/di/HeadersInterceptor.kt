package `in`.nakkalites.mediaclient.app.di

import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.data.HttpStatus.LOGOUT
import `in`.nakkalites.mediaclient.data.HttpStatus.UNAUTHORIZED
import `in`.nakkalites.mediaclient.data.user.UserService
import `in`.nakkalites.mediaclient.domain.login.UserDataStore
import `in`.nakkalites.mediaclient.domain.utils.LogoutHandler
import `in`.nakkalites.mediaclient.view.utils.isValidApiUrl
import `in`.nakkalites.mediaclient.view.utils.md5Base64
import `in`.nakkalites.mediaclient.view.utils.runOnUiThread
import android.os.Bundle
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import retrofit2.HttpException
import timber.log.Timber

class HeadersInterceptor(
    private val userDataStore: UserDataStore,
    private val userService: Lazy<UserService>,
    private val logoutHandler: Lazy<LogoutHandler>,
    private val analyticsManager: Lazy<AnalyticsManager>,
) : Interceptor {
    private val maxRetries = 3
    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        if (proceedToNextInterceptor(chain)) return chain.proceed(chain.request())

        val origRequest = chain.request()
        var response = chain.proceed(rewriteHeaders(origRequest))
        var count = 1
        while (count < maxRetries) {
            if (response.code == UNAUTHORIZED) {
                val url = response.request.url
                track401Received(url.encodedPath)
            } else {
                return response
            }
            synchronized(lock) {
                try {
                    if (isTokenRefreshAlready(response.request)) return@synchronized
                    refreshAccessToken()
                } catch (e: Exception) {
                    if (e is HttpException && e.code() == LOGOUT) {
                        return logoutResponse(origRequest, e)
                    }
                }
            }
            response.close()
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
        .headers(HeadersFactory(userDataStore).get().toHeaders())
        .build()

    private fun isTokenRefreshAlready(originalRequest: Request): Boolean {
        val storedToken = userDataStore.getAccessToken()
        return storedToken.isNotEmpty()
                && originalRequest.header(Headers.APP_ACCESS_TOKEN) != storedToken
    }

    private fun refreshAccessToken() {
        try {
            val refreshToken = userDataStore.getRefreshToken()
            val headers = HeadersFactory(userDataStore).get()
            val params = mapOf<String, Any>(
                "refresh_token" to refreshToken
            )
            userService.value.refreshToken(headers, params).blockingGet().let {
                if (it.isSuccessful) {
                    userDataStore.setAccessToken(it.body()?.accessToken)
                    userDataStore.setRefreshToken(it.body()?.refreshToken)
                } else if (it.code() == LOGOUT) {
                    logoutHandler.value.logout()
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun track401Received(encodedPath: String) = runOnUiThread {
        val eventName = AnalyticsConstants.Event._401_CODE_RECEIVED
        val properties = Bundle().apply {
            putString(AnalyticsConstants.Property.XO, userDataStore.getAccessToken().md5Base64())
            putString(AnalyticsConstants.Property.OX, userDataStore.getRefreshToken().md5Base64())
            putString(AnalyticsConstants.Property.INSTANCE_ID, userDataStore.getInstanceIdOrEmpty())
            putString(AnalyticsConstants.Property.API_ENDPOINT, encodedPath)
        }
        analyticsManager.value.logEvent(eventName, properties)
    }
}

fun logoutResponse(origRequest: Request, error: HttpException) = Response.Builder()
    .request(origRequest)
    .protocol(Protocol.HTTP_2)
    .body(error.response()?.errorBody())
    .apply {
        error.response()?.message()?.let { message(it) }
    }
    .code(LOGOUT)
    .build()
