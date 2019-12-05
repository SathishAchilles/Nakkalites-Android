package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.view.utils.dpToPx
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

object Bindings {
    @JvmStatic
    @BindingAdapter("android:onClick")
    fun View.bindOnClick(onClickRunnable: Function0<Unit>?) {
        setOnClickListener { onClickRunnable?.invoke() }
    }

    @JvmStatic
    @BindingAdapter("android:visibility")
    fun View.bindVisibility(visibleOrGone: Boolean) {
        visibility = if (visibleOrGone) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter(
        value = ["imageUrl", "imageUri", "fit", "resizeWidth",
            "resizeHeight", "centerCrop", "centerInside", "imageTransforms", "bitmapConfig"],
        requireAll = false
    )
    fun ImageView.bindLoadImage(
        url: String?, uri: Uri?, fit: Boolean, resizeWidth: Int?, resizeHeight: Int?,
        centerCrop: Boolean, centerInside: Boolean, transforms: List<Transformation>?,
        bitmapConfig: Bitmap.Config?
    ) {
        if (url == null && uri == null) {
            return
        }
        val imageUri = uri ?: Uri.parse(url)
        val rc = Picasso.get().load(imageUri)

        if (resizeWidth != null && resizeHeight != null) {
            rc.resize(resizeWidth, resizeHeight)
        }
        if (fit) {
            rc.fit()
        }
        if (centerCrop) {
            rc.centerCrop()
        }
        if (centerInside) {
            rc.centerInside()
        }
        if (transforms != null) {
            rc.transform(transforms)
        }
        if (bitmapConfig != null) {
            rc.config(bitmapConfig)
        }
        rc.into(this)
        loge("Image loaded")
    }

    @JvmStatic
    @BindingAdapter("bringToFront")
    fun View.bindBringToFront(bringToFront: Boolean) {
        if (bringToFront) bringToFront()
    }

    @BindingMethods(
        BindingMethod(
            type = TabLayout::class, attribute = "onTabSelect", method = "addOnTabSelectedListener"
        )
    )
    class TabLayoutBindingAdapter

    @JvmStatic
    @BindingAdapter("width")
    fun View.bindWidth(@Px width: Int) {
        val lp = layoutParams
        lp.width = width
        layoutParams = lp
    }

    @JvmStatic
    @BindingAdapter("height")
    fun View.bindHeight(height: Float) {
        if (height >= ViewGroup.LayoutParams.WRAP_CONTENT) {
            val lp = layoutParams
            lp.height = height.toInt()
            layoutParams = lp
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["textRes"])
    fun TextView.bindTextWithFormat(@StringRes stringRes: Int?) {
        stringRes?.let {
            text = context.getString(it)
        }
    }

    @JvmStatic
    @BindingAdapter(
        value = ["android:drawableLeft", "android:drawableTop", "android:drawableRight", "android:drawableBottom", "android:drawableTint"],
        requireAll = false
    )
    fun TextView.bindCompoundDrawables(
        left: Drawable, top: Drawable, right: Drawable, bottom: Drawable, @ColorInt color: Int
    ) {
        val input =
            arrayOf(left, top, right, bottom)
        val output: Array<Drawable?> =
            input.map { drawable -> tintDrawable(drawable, color) }.toTypedArray()
        setCompoundDrawablesWithIntrinsicBounds(output[0], output[1], output[2], output[3])
    }

    @JvmStatic
    @BindingAdapter("android:layout_marginStart")
    fun View.bindMarginLeft(marginStart: Int) {
        val lp = layoutParams as MarginLayoutParams
        lp.leftMargin = marginStart
        layoutParams = lp
    }

    private fun tintDrawable(drawable: Drawable?, @ColorInt color: Int): Drawable? {
        if (drawable != null && color != 0) {
            val wrapped = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrapped.mutate(), color)
            return wrapped
        }
        return drawable
    }
}
