package `in`.nakkalites.mediaclient.app

import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.di.applicationModule
import `in`.nakkalites.mediaclient.app.di.netModule
import `in`.nakkalites.mediaclient.app.di.viewModelModule
import `in`.nakkalites.mediaclient.app.utils.RxErrorHandler
import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.domain.login.RefreshTokenManager
import `in`.nakkalites.mediaclient.domain.login.UserManager
import `in`.nakkalites.mediaclient.domain.utils.LogoutHandler
import `in`.nakkalites.mediaclient.view.NonFatalReportingTree
import android.R
import android.app.Application
import androidx.core.app.NotificationManagerCompat
import com.freshchat.consumer.sdk.Freshchat
import com.freshchat.consumer.sdk.FreshchatConfig
import com.freshchat.consumer.sdk.FreshchatNotificationConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree


class NakkalitesApp : Application() {
    private val debug = BuildConfig.DEBUG
    val userManager: UserManager by inject()
    val logoutHandler: LogoutHandler by inject()
    val crashlytics: FirebaseCrashlytics by inject()
    val freshchat: Freshchat by inject()

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
        Timber.plant(
            if (BuildConfig.DEBUG) {
                DebugTree()
            } else {
                NonFatalReportingTree(crashlytics)
            }
        )
        val refreshTokenManager: RefreshTokenManager = get()
        val config = FreshchatConfig(
            "b14443fa-5ba3-4999-9f19-fae0cdce1e40",
            "2664f834-e43e-4eea-b31f-020cf4c47007"
        )
        config.domain = "msdk.in.freshchat.com"
        config.isCameraCaptureEnabled = false
        config.isGallerySelectionEnabled = false
        config.isResponseExpectationEnabled = false
        freshchat.init(config)
        val userMeta: MutableMap<String, String> = HashMap()
        userMeta["user_id"] = userManager.getUser()?.id.toString()
        freshchat.setUserProperties(userMeta)
        val notificationConfig = FreshchatNotificationConfig()
            .setNotificationSoundEnabled(true)
            .setImportance(NotificationManagerCompat.IMPORTANCE_MAX)
        freshchat.setNotificationConfig(notificationConfig)
    }

    private val serverUrl: String
        get() = if (debug) {
            HttpConstants.BASE_URL_DEV
        } else {
            HttpConstants.BASE_URL_PROD
        }
}
