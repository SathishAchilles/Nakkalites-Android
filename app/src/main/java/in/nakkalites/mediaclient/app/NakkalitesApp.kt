package `in`.nakkalites.mediaclient.app

import `in`.nakkalites.logging.initDebugLogs
import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.app.constants.HttpConstants
import `in`.nakkalites.mediaclient.app.di.applicationModule
import `in`.nakkalites.mediaclient.app.di.netModule
import `in`.nakkalites.mediaclient.app.di.viewModelModule
import android.app.Application
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NakkalitesApp : Application() {
    private val debug = BuildConfig.DEBUG

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@NakkalitesApp)
            modules(
                listOf(applicationModule, viewModelModule, netModule(serverUrl))
            )
        }
        if (!debug) {
            Fabric.with(this, Crashlytics())
//            if (user.valid()) {
//                Crashlytics.setUserIdentifier(String.valueOf(user.userId()))
//            }
        }
        initDebugLogs()
    }

    private val serverUrl: String
        get() = if (debug) {
            HttpConstants.BASE_URL_DEV
        } else {
            HttpConstants.BASE_URL_PROD
        }
}
