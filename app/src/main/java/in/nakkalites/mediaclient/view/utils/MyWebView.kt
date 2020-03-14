package `in`.nakkalites.mediaclient.view.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView

/**
 * The factory version of WebView (Version 37) which ships with Android 5 and 5.1 crashes during
 * layout inflation when a custom Locale is set.
 *
 * To reproduce this crash, it's necessary to reset WebView app to its factory version in Android 5 and 5.1.
 *
 * References:
 * 1. https://stackoverflow.com/q/41025200/1852422
 * 3. https://bugs.chromium.org/p/chromium/issues/detail?id=521753#c8
 */
class MyWebView : WebView {
    constructor(context: Context) : super(getFixedContext(context))

    constructor(context: Context, attrs: AttributeSet) : super(getFixedContext(context), attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(getFixedContext(context), attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        getFixedContext(context), attrs, defStyleAttr, defStyleRes
    )

    companion object {
        @SuppressLint("NewApi")
        @JvmStatic
        fun getFixedContext(context: Context): Context = when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1 -> context.createConfigurationContext(
                Configuration()
            )
            else -> context
        }
    }
}
