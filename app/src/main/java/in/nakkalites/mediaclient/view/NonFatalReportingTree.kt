package `in`.nakkalites.mediaclient.view

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * [Log.ERROR] and [Log.WARN] are written to both logcat and Crashlytics.
 * [Log.INFO] is only written to Crashlytics.
 * Others ignored.
 *
 * @see [
 * Fabric Custom Logging](https://docs.fabric.io/android/crashlytics/enhanced-reports.html?.custom-logging)
 */
class NonFatalReportingTree internal constructor(
    private val crashlytics: FirebaseCrashlytics
) : Timber.Tree() {
    override fun log(
        priority: Int, tag: String?, message: String, t: Throwable?
    ) {
        if (!(priority == Log.ERROR || priority == Log.WARN || priority == Log.INFO)) {
            return
        }
        if (t != null) {
            crashlytics.recordException(t)
        }
        when (priority) {
            Log.ERROR -> {
                crashlytics.log(message)
                Log.println(Log.ERROR, tag, message)
            }
            Log.WARN -> {
                crashlytics.log(message)
                Log.println(Log.WARN, tag, message)
            }
            Log.INFO -> crashlytics.log(message)
        }
    }
}
