package `in`.nakkalites.mediaclient.view.utils

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.view.widgets.RoundedCornersTransformation
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Transformation
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import okhttp3.Request
import retrofit2.Retrofit
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

inline fun <reified T> Retrofit.create(): T {
    return create(T::class.java)
}

private val Request.apiRegex: Regex by lazy { Regex("^[\\w.-]+\\.herokuapp\\.com\$") }

fun Request.isValidApiUrl(): Boolean {
    return apiRegex.matches(url.host)
}

@Suppress("NOTHING_TO_INLINE")
inline fun argumentError(message: Any? = null): Nothing =
    throw IllegalArgumentException(message?.toString())

fun SwipeRefreshLayout.setDefaultColors() {
    setColorSchemeColors(ContextCompat.getColor(context, R.color.progress_bar))
}

fun getDefaultTransformations(): List<Transformation> =
    listOf<Transformation>(RoundedCornersTransformation(20, 0))

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

fun Activity.playStoreIntent() = Intent(
    Intent.ACTION_VIEW, Uri.parse(AppConstants.PLAY_STORE_URL + packageName)
)

fun Activity.playStoreUrl() = AppConstants.PLAY_STORE_COMPLETE_URL

fun Activity.setPortraitOrientation() {
    var uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
    window.decorView.systemUiVisibility = uiOptions
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

fun Activity.setLandScapeOrientation() {
    val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    window.decorView.systemUiVisibility = uiOptions
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
}

fun shareTextIntent(shareTitle: String, shareText: String): Intent =
    Intent(Intent.ACTION_SEND)
        .setType("text/*")
        .putExtra(Intent.EXTRA_TEXT, shareText)
        .let { Intent.createChooser(it, shareTitle) }

fun ContentResolver.isRotationEnabled() =
    Settings.System.getInt(this, Settings.System.ACCELEROMETER_ROTATION, 0) == 1

fun getTimeStampForAnalytics(): String? {
    val dateFormat: DateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    return dateFormat.format(Date())
}

fun String.formatEn(vararg args: Any?): String = format(Locale.US, *args)

fun commitAllowingStateLoss(fragment: Fragment, fm: FragmentManager, tag: String) {
    fm.beginTransaction()
        .add(fragment, tag)
        .commitAllowingStateLoss();
}

fun Phonenumber.PhoneNumber.getIsoCode(phoneNumberUtil: PhoneNumberUtil) =
    phoneNumberUtil.getRegionCodeForCountryCode(this.countryCode)

fun Phonenumber.PhoneNumber.getNumberWithCountryCode() = "+${this.countryCode}${this.nationalNumber}"


fun showSoftKeyboard(view: View) {
    val imm = view.context
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}
