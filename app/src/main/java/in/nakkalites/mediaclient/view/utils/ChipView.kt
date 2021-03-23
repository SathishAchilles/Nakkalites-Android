package `in`.nakkalites.mediaclient.view.utils

import `in`.nakkalites.mediaclient.R
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip

open class ChipView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : Chip(context, attrs, R.attr.chipChoice) {

    fun setChipBackgroundColorRes(@ColorRes resId: Int?) {
        val validResId = resId.toValidResId() ?: return
        setChipBackgroundColorResource(validResId)
    }

    fun setChipStrokeColorRes(@ColorRes resId: Int?) {
        val validResId = resId.toValidResId() ?: return
        setChipStrokeColorResource(validResId)
    }

    fun setChipTextColorRes(@ColorRes resId: Int?) {
        val validResId = resId.toValidResId() ?: return
        setTextColor(
            ContextCompat.getColorStateList(context, validResId)
        )
    }

    fun setChipStrokeWidthRes(@DimenRes resId: Int?) {
        val validResId = resId.toValidResId() ?: return
        setChipStrokeWidthResource(validResId)
    }

    fun setTextAppearanceRes(@StyleRes resId: Int?) {
        val validResId = resId.toValidResId() ?: return
        setTextAppearanceResource(validResId)
    }

    fun setChipIconRes(@DrawableRes resId: Int?) {
        val validResId = resId.toValidResId() ?: return
        setChipIconResource(validResId)
    }

    fun setChipIconSizeRes(@DimenRes resId: Int?) {
        val validResId = resId.toValidResId() ?: return
        setChipIconSizeResource(validResId)
    }

    fun setChipIconTintSizeRes(@ColorRes resId: Int?) {
        val validResId = resId.toValidResId() ?: return
        setChipIconTintResource(validResId)
    }
}

internal fun Int?.toValidResId(): Int? = if (this == 0) null else this
