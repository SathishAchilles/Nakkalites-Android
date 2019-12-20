package `in`.nakkalites.mediaclient.app.utils

import androidx.recyclerview.widget.RecyclerView
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException


/**
 * Global error handler for RxJava to call when exceptions cannot be delivered to Observer's onError.
 *
 *
 * Examples include:
 * 1. Exception thrown from Observer's onNext.
 * 2. [CompositeException] thrown when Observer's onError throws an Exception.
 * 3. [UndeliverableException] thrown when both Singles fail after a `Single#zip`.
 * (so resulting in one Exception delivered to Observer's onError and other to this global handler)
 *
 *
 * See https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling,
 * [RxJavaPlugins.onError], [RxJavaPlugins.isBug]
 */
object RxErrorHandler {
    fun create(): Consumer<Throwable> {
        return Consumer { e: Throwable ->
            val cause: Throwable? =
                if (e is UndeliverableException) {
                    e.cause!!
                } else {
                    null
                }
            Timber.e(cause)
            if (!swallowRxException(e)) {
                throwUncaughtException(e)
            }
        }
    }

    /**
     * For exception thrown from RecyclerView, see [RecyclerView.assertNotInLayoutOrScroll]
     */
    private fun swallowRxException(e: Throwable): Boolean {
        val recyclerViewMsg = ("Cannot call this method while RecyclerView is "
                + "computing a layout or scrolling")
        val recyclerViewWarning =
            e.message != null && e.message!!.contains(recyclerViewMsg)
        val ioOrNetworkIssue =
            e is IOException || e is HttpException
        // some blocking code was interrupted by a dispose call
        val threadInterrupt = e is InterruptedException
        return recyclerViewWarning || ioOrNetworkIssue || threadInterrupt
    }

    private fun throwUncaughtException(throwable: Throwable) {
        val currentThread = Thread.currentThread()
        currentThread.uncaughtExceptionHandler.uncaughtException(currentThread, throwable)
    }
}

