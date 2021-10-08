package `in`.nakkalites.mediaclient.app.di

import `in`.nakkalites.mediaclient.data.HttpStatus.LOGOUT
import `in`.nakkalites.mediaclient.data.HttpStatus.UNAUTHORIZED
import `in`.nakkalites.mediaclient.data.user.UserService
import `in`.nakkalites.mediaclient.domain.login.UserDataStore
import `in`.nakkalites.mediaclient.domain.utils.LogoutHandler
import `in`.nakkalites.mediaclient.view.utils.isValidApiUrl
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import retrofit2.HttpException
import timber.log.Timber

class HeadersInterceptor(
    private val userDataStore: UserDataStore,
    private val userService: Lazy<UserService>,
    private val logoutHandler: Lazy<LogoutHandler>
) : Interceptor {
    private val maxRetries = 3
    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        if (proceedToNextInterceptor(chain)) return chain.proceed(chain.request())

        val origRequest = chain.request()
        var response = chain.proceed(rewriteHeaders(origRequest))
        var count = 1
        while (count < maxRetries) {
            if (response.code != UNAUTHORIZED) return response
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
