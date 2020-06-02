package `in`.nakkalites.mediaclient.app

import `in`.nakkalites.logging.initDebugLogs
import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.di.applicationModule
import `in`.nakkalites.mediaclient.app.di.netModule
import `in`.nakkalites.mediaclient.app.di.viewModelModule
import `in`.nakkalites.mediaclient.app.utils.RxErrorHandler
import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.domain.utils.LogoutHandler
import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NakkalitesApp : Application() {
    private val debug = BuildConfig.DEBUG
    val userManager: UserManager by inject()
    val logoutHandler: LogoutHandler by inject()
    val crashlytics: FirebaseCrashlytics by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@NakkalitesApp)
            modules(listOf(applicationModule, viewModelModule, netModule(serverUrl)))
        }
        userManager.getUser()?.let {
            crashlytics.setUserId(it.id)
            if (it.email != null) {
                crashlytics.setCustomKey(AppConstants.USER_EMAIL, it.email)
            }
        }
        RxJavaPlugins.setErrorHandler(RxErrorHandler.create()) // only for UndeliverableExceptions
        initDebugLogs()
    }

    private val serverUrl: String
        get() = if (debug) {
            HttpConstants.BASE_URL_DEV
        } else {
            HttpConstants.BASE_URL_PROD
        }
}
