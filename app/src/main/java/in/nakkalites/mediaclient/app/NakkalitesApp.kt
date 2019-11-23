package `in`.nakkalites.mediaclient.app

import `in`.nakkalites.mediaclient.BuildConfig
import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import `in`.nakkalites.logging.initDebugLogs
import `in`.nakkalites.mediaclient.app.di.applicationModule
import `in`.nakkalites.mediaclient.app.di.netModule
import `in`.nakkalites.mediaclient.app.di.picassoModule
import `in`.nakkalites.mediaclient.app.di.viewModelModule
import com.facebook.stetho.Stetho
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
            modules(listOf(applicationModule, viewModelModule, netModule, picassoModule))
        }
        if (!debug) {
            Fabric.with(this, Crashlytics())
//            if (user.valid()) {
//                Crashlytics.setUserIdentifier(String.valueOf(user.userId()))
//            }
            Stetho.initializeWithDefaults(this)
        }
        initDebugLogs()
    }
}
