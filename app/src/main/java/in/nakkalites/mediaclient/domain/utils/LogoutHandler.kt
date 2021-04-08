package `in`.nakkalites.mediaclient.domain.utils

import `in`.nakkalites.logging.logd
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.view.login.LoginActivity
import android.content.Context
import android.content.Intent
import com.razorpay.Checkout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class LogoutHandler(private val context: Context, private val userManager: UserManager) {
    private val logoutSubject = PublishSubject.create<Unit>()

    @Suppress("unused") // App scope.
    private val logoutDisposable = logoutSubject.debounce(2, TimeUnit.SECONDS)
        .observeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(onNext = {
            clearAppData(context, userManager)
            val intent = LoginActivity.createIntent(context)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
            logd("Opening LoginActivity after logging out")
        })

    fun logout() {
        logd("Queuing logout")
        logoutSubject.onNext(Unit)
    }

    private fun clearAppData(context: Context, userManager: UserManager) {
        userManager.clearAppData()
        userManager.generateInstanceIdIfNotAvailable()
        Checkout.clearUserData(context)
    }
}
