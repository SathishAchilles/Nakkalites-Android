package `in`.nakkalites.mediaclient.domain.utils

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.data.HttpStatus
import android.app.Activity
import android.widget.Toast
import retrofit2.HttpException
import timber.log.Timber

@JvmOverloads
fun Activity.errorHandler(error: Throwable, shouldHandleError: () -> Boolean = { true }) {
    Timber.e(error)
    when {
        error is HttpException && error.code() == HttpStatus.LOGOUT -> {
            Timber.e("Logout ${error.code()}")
            /*logout()*/
        }
        else -> if (shouldHandleError()) {
            Toast.makeText(this, R.string.generic_error_message, Toast.LENGTH_SHORT).show()
        }
    }
}
