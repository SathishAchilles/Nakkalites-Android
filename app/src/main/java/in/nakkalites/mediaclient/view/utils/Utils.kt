package `in`.nakkalites.mediaclient.view.utils

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.view.widgets.RoundedCornersTransformation
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Transformation
import okhttp3.Request
import retrofit2.Retrofit

inline fun <reified T> Retrofit.create(): T {
    return create(T::class.java)
}

private val Request.apiRegex: Regex by lazy { Regex("^[\\w.-]+\\.nakkalites\\.in\$") }
fun Request.isValidApiUrl(): Boolean {
    return apiRegex.matches(url.host)
}

@Suppress("NOTHING_TO_INLINE")
inline fun argumentError(message: Any? = null): Nothing =
    throw IllegalArgumentException(message?.toString())

fun SwipeRefreshLayout.setDefaultColors() {
    setColorSchemeColors(ContextCompat.getColor(context, R.color.cyan_400))
}

fun getDefaultTransformations(): List<Transformation> =
    listOf<Transformation>(RoundedCornersTransformation(50, 0))

fun Context.dpToPx(dp: Int): Int {
    val density = resources.displayMetrics.density
    return (dp * density).toInt()
}

fun Activity.displayDimens() =
    Point().apply {
        this@displayDimens.windowManager.defaultDisplay.getSize(this)
    }

fun Activity.displayWidth() = displayDimens().x

fun Activity.displayHeight() = displayDimens().y

fun runOnUiThread(runnable: Runnable?) {
    Handler(Looper.getMainLooper())
        .post(runnable)
}

fun Activity.playStoreIntent() = Intent(
    Intent.ACTION_VIEW, Uri.parse(AppConstants.PLAY_STORE_URL + packageName)
)

