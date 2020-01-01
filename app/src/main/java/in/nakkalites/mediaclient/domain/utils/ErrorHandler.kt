package `in`.nakkalites.mediaclient.domain.utils

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.NakkalitesApp
import `in`.nakkalites.mediaclient.data.HttpStatus
import `in`.nakkalites.mediaclient.view.utils.runOnUiThread
import `in`.nakkalites.mediaclient.viewmodel.utils.NoUserFoundException
import android.app.Activity
import android.widget.Toast
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

@JvmOverloads
fun Activity.errorHandler(error: Throwable, shouldHandleError: () -> Boolean = { false }) {
    Timber.e(error)
    when {
//        error is HttpException && error.code() == HttpStatus.LOGOUT -> logout()
        else -> if (shouldHandleError()) {
            runOnUiThread {
                Toast.makeText(this, R.string.generic_error_message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
