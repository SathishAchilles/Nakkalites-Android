package `in`.nakkalites.mediaclient.app

import `in`.nakkalites.mediaclient.BuildConfig
import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import `in`.nakkalites.logging.initDebugLogs
import `in`.nakkalites.mediaclient.app.di.applicationModule
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
            modules(applicationModule)
        }
//        if (!debug) {
        Fabric.with(this, Crashlytics())
//            if (user.valid()) {
//                Crashlytics.setUserIdentifier(String.valueOf(user.userId()))
//            }
//        }
        initDebugLogs()
    }
}
