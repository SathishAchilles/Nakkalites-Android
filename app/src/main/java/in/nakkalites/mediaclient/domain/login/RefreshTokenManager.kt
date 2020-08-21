package `in`.nakkalites.mediaclient.domain.login

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.app.di.HeadersFactory
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject

class RefreshTokenManager(
    refreshTokenSubject: PublishSubject<RefreshTokenCallback>,
    private val userManager: UserManager,
    private val headersFactory: HeadersFactory
) {
    init {
        val disposable = refreshTokenSubject
            .subscribeOn(mainThread())
            .observeOn(io())
            .subscribeBy(onNext = { refreshAccessToken(it) },
                onError = { loge("RefreshTokenManager failed", throwable = it) })
    }

    private fun refreshAccessToken(refreshTokenCallback: RefreshTokenCallback) {
        val refreshToken = userManager.getRefreshToken()
        val headers = headersFactory.get()
        val response = userManager.refreshToken(headers, refreshToken).blockingGet()
        userManager.setAccessToken(response.accessToken)
        userManager.setRefreshToken(response.refreshToken)
        refreshTokenCallback.callback.invoke()
    }
}

class RefreshTokenCallback(val callback: () -> Unit)
