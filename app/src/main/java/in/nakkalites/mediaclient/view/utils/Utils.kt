package `in`.nakkalites.mediaclient.view.utils

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.view.widgets.RoundedCornersTransformation
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Transformation
import okhttp3.Request
import retrofit2.Retrofit

inline fun <reified T> Retrofit.create(): T {
    return create(T::class.java)
}

private val Request.apiRegex: Regex by lazy { Regex("^[\\w.-]+\\.meeshoapi\\.com\$") }
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
