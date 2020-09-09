package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.app.di.HeadersFactory
import `in`.nakkalites.mediaclient.data.HttpStatus
import `in`.nakkalites.mediaclient.domain.utils.LogoutHandler
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException

class RefreshTokenManager(
    refreshTokenSubject: PublishSubject<RefreshTokenCallback>,
    private val userManager: UserManager,
    private val userDataStore: UserDataStore, private val logoutHandler: LogoutHandler
) {
    init {
        refreshTokenSubject
            .subscribeOn(mainThread())
            .observeOn(io())
            .subscribeBy(
                onNext = {
                    val callback = it.callback
                    try {
                        refreshAccessToken()
                        callback.invoke()
                    } catch (e: Exception) {
                        if (e is HttpException && e.code() == HttpStatus.LOGOUT) {
                            callback.invoke()
                            logoutHandler.logout()
                        } else {
                            callback.invoke()
                        }
                    }
                },
                onError = { loge("RefreshTokenManager failed", throwable = it) })
    }

    @Throws(HttpException::class)
    private fun refreshAccessToken() {
        val refreshToken = userManager.getRefreshToken()
        val headers = HeadersFactory(userDataStore).get()
        userManager.refreshToken(headers, refreshToken).blockingGet()
    }
}

class RefreshTokenCallback(val callback: () -> Unit)
