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
import java.util.regex.Matcher
import java.util.regex.Pattern

inline fun <reified T> Retrofit.create(): T {
    return create(T::class.java)
}

private val Request.apiRegex: Regex by lazy { Regex("^[\\w.-]+\\.onrender\\.com\$") }
private val Request.apiRegex1: Regex by lazy { Regex("^[\\w.-]+\\.nakkalites\\.app\$") }
private val Request.apiRegex2: Regex by lazy { Regex("^[\\w.-]+\\.nakkalites\\.in\$") }

fun Request.isValidApiUrl(): Boolean {
    return apiRegex.matches(url.host) || apiRegex1.matches(url.host) || apiRegex2.matches(url.host)
}

val VALID_EMAIL_ADDRESS_REGEX: Pattern =
    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)

fun validateEmail(emailStr: String): Boolean {
    val matcher: Matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr)
    return matcher.find()
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

fun playStoreUrl() = AppConstants.PLAY_STORE_COMPLETE_URL

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

fun Phonenumber.PhoneNumber.getNumberWithCountryCode() =
    "+${this.countryCode}${this.nationalNumber}"


fun showSoftKeyboard(view: View) {
    val imm = view.context
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}
