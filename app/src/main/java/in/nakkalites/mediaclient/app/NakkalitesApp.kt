package `in`.nakkalites.mediaclient.app

import `in`.nakkalites.logging.initDebugLogs
import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.app.di.applicationModule
import `in`.nakkalites.mediaclient.app.di.netModule
import `in`.nakkalites.mediaclient.app.di.viewModelModule
import `in`.nakkalites.mediaclient.app.utils.RxErrorHandler
import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.domain.login.UserManager
import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NakkalitesApp : Application() {
    private val debug = BuildConfig.DEBUG
    val userManager: UserManager by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@NakkalitesApp)
            modules(listOf(applicationModule, viewModelModule, netModule(serverUrl)))
        }
        if (!debug) {
            Fabric.with(this, Crashlytics())
            userManager.getUser()?.let { Crashlytics.setUserIdentifier(it.id) }
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
