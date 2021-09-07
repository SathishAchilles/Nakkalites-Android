package `in`.nakkalites.mediaclient.viewmodel.utils

import `in`.nakkalites.mediaclient.R
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.FontRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import timber.log.Timber


class StyleFormatText {
    val plainText: String?
    val displayText: DisplayText?
    @ColorRes var color: Int? = null
    @DimenRes var textSize: Int? = null
    @StyleRes val textAppearanceRes: Int?

    constructor(plainText: String) {
        this.plainText = plainText
        this.displayText = null
        this.textAppearanceRes = null
    }

    @JvmOverloads
    constructor(displayText: DisplayText, @StyleRes textAppearanceRes: Int? = null) {
        this.plainText = null
        this.displayText = displayText
        this.textAppearanceRes = textAppearanceRes
    }
}

/**
 * Basic support for transforming text with style info into a [Spannable].
 * @return a [Spannable] containing bold [StyleSpan]s for the text surrounded by * chars.
 */
@JvmName("createSpannable")
fun StyleFormatText.toSpannable(ctx: Context): Spannable {
    val text = when {
        plainText != null -> plainText
        displayText != null -> displayText.getText(ctx.resources)
        else -> error("Both plainText and displayText are null.")
    }
    val boldTypeface = getFontSafely(ctx, R.font.inter_bold)
    val builder = SpannableStringBuilder()
    var lastChar: Char? = null
    var boldStr: StringBuilder? = null
    val backslash = '\\' // Backslash char itself needs escaping!
    for (i in 0 until text.count()) {
        val char = text[i]
        val isStar = char == '*' && lastChar != backslash
        when {
            char == backslash && lastChar != backslash -> {
                // Escape sequence started, so nothing to do.
            }
            isStar && boldStr != null -> {
                val spanStart = builder.length
                builder.append(boldStr)
                val textAppTextAppearanceSpan = textAppearanceRes?.let { TextAppearanceSpan(ctx, it) }
                if (textAppTextAppearanceSpan != null) {
                    builder.setSpan(
                        textAppTextAppearanceSpan, spanStart, builder.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                } else {
                    val typefaceSpan = boldTypeface?.let(::CustomTypefaceSpan)
                    if (typefaceSpan != null) {
                        builder.setSpan(
                            typefaceSpan, spanStart, builder.length,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                val foregroundColorSpan = color?.let { colorRes ->
                    ForegroundColorSpan(ContextCompat.getColor(ctx, colorRes))
                }
                if (foregroundColorSpan != null) {
                    builder.setSpan(
                        foregroundColorSpan, spanStart, builder.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                val absoluteSizeSpan = textSize?.let { sizeRes ->
                    AbsoluteSizeSpan(ctx.resources.getDimension(sizeRes).toInt())
                }
                if (absoluteSizeSpan != null) {
                    builder.setSpan(
                        absoluteSizeSpan, spanStart, builder.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                boldStr = null
            }
            boldStr != null -> boldStr.append(char)
            isStar -> boldStr = StringBuilder()
            char != backslash || lastChar == backslash -> builder.append(char)
        }
        lastChar = char
    }
    return builder
}

fun createColorSpannableString(
    context: Context,
    str: String,
    start: Int,
    end: Int,
    @ColorRes colorId: Int
): SpannableString {
    val spannableString =
        SpannableString(str)
    spannableString.setSpan(
        ForegroundColorSpan(
            ContextCompat.getColor(context, colorId)
        ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return spannableString
}

/**
 * ResourcesCompat.getFont() fails with [Resources.NotFoundException] on phones without
 * Play Services. Use this API instead to get a null instead of that exception.
 */
fun getFontSafely(ctx: Context, @FontRes id: Int): Typeface? {
    return try {
        ResourcesCompat.getFont(ctx, id)
    } catch (e: RuntimeException) {
        Timber.e(e)
        null
    }
}
