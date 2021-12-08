package `in`.nakkalites.logging

import `in`.nakkalites.logging.BuildConfig.DEBUG
import android.util.Log
import timber.log.Timber

/**
 * Includes given block of code in [action] only on debug builds.
 *
 * @param include to indicate if the block should be
 */
inline fun onDebug(include: Boolean = DEBUG, action: () -> Unit) {
    if (include) {
        action()
    }
}

/**
 * Logs with [Log.DEBUG] level when [include] is `true`
 */
inline fun logd(
    message: String,
    include: Boolean = DEBUG,
    throwable: Throwable? = null,
    vararg args: Any
) {
    if (include) {
        throwable?.let { Timber.d(it, message, args) } ?: Timber.d(message, args)
    }
}

/**
 * Logs with [Log.VERBOSE] level when [include] is `true`
 */
inline fun logv(
    message: String,
    include: Boolean = DEBUG,
    throwable: Throwable? = null,
    vararg args: Any
) {
    if (include) {
        throwable?.let { Timber.v(it, message, args) } ?: Timber.v(message, args)
    }
}

/**
 * Logs with [Log.INFO] level when [include] is `true`
 */
inline fun logi(
    message: String,
    include: Boolean = DEBUG,
    throwable: Throwable? = null,
    vararg args: Any
) {
    if (include) {
        throwable?.let { Timber.i(it, message, args) } ?: Timber.i(message, args)
    }
}


/**
 * Logs with [Log.INFO] level when [include] is `true`
 */
inline fun loge(
    message: String,
    throwable: Throwable? = null,
    vararg args: Any
) {
    throwable?.let { Timber.e(it, message, args) } ?: Timber.e(message, args)
}

/**
 * Logs the given [throwable] with no message attached.
 */
inline fun logThrowable(throwable: Throwable) = loge(message = "", throwable = throwable)
