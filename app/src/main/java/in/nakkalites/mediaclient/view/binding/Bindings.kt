package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
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
    fun View.bindHeight(@Px height: Float) {
        if (height >= ViewGroup.LayoutParams.WRAP_CONTENT) {
            val lp = layoutParams
            lp.height = height.toInt()
            layoutParams = lp
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["textRes"])
    fun TextView.bindTextWithFormat(@StringRes stringRes: Int?) {
        stringRes?.apply {
            text = context.getString(this)
        }
    }

    @JvmStatic
    @BindingAdapter(
        value = ["android:drawableLeft", "android:drawableTop", "android:drawableRight", "android:drawableBottom", "android:drawableTint"],
        requireAll = false
    )
    fun TextView.bindCompoundDrawables(
        left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?, @ColorInt color: Int
    ) {
        val input =
            arrayOf(left, top, right, bottom)
        val output: Array<Drawable?> =
            input.map { drawable -> tintDrawable(drawable, color) }.toTypedArray()
        setCompoundDrawablesWithIntrinsicBounds(output[0], output[1], output[2], output[3])
    }

    @JvmStatic
    @BindingAdapter(
        value = ["android:drawableLeft", "android:drawableTop", "android:drawableRight", "android:drawableBottom", "android:drawableTint"],
        requireAll = false
    )
    fun TextView.bindCompoundDrawablesRes(
        left: Int, top: Int, right: Int, bottom: Int, @ColorInt color: Int
    ) {
        bindCompoundDrawables(
            getDrawable(context, left), getDrawable(context, top),
            getDrawable(context, right), getDrawable(context, bottom), color
        )
    }

    private fun getDrawable(ctx: Context, drawableResId: Int): Drawable? {
        return if (drawableResId == 0) null else AppCompatResources.getDrawable(ctx, drawableResId)
    }

    @JvmStatic
    @BindingAdapter("android:layout_marginStart")
    fun View.bindMarginStart(@DimenRes marginStart: Int) {
        val lp = layoutParams as MarginLayoutParams
        lp.leftMargin = context.resources.getDimension(marginStart).toInt()
        layoutParams = lp
    }

    @JvmStatic
    @BindingAdapter(value = ["spanSizeLookup"])
    fun RecyclerView.bindSpanSizeLookup(spanSizeLookup: SpanSizeLookup?) {
        spanSizeLookup?.apply {
            val gridLm = layoutManager as GridLayoutManager
            gridLm.spanSizeLookup = this
        }
    }

    @JvmStatic
    @BindingAdapter("spanCount")
    fun RecyclerView.bindSpanCount(spanCount: Int) {
        val gridLm = layoutManager as GridLayoutManager
        gridLm.spanCount = spanCount
    }

    @JvmStatic
    @BindingAdapter("android:text")
    fun TextView.bindDisplayText(displayText: DisplayText?) {
        text = displayText?.getText(resources)
    }

    @JvmStatic
    @BindingAdapter("android:tint")
    fun ImageView.bindImageViewSrcTint(@ColorInt color: Int?) {
        color?.let {
            ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
        }
    }

    @JvmStatic
    @BindingAdapter("android:src")
    fun ImageView.bindImageSrc(@DrawableRes resId: Int) {
        val drawable = getDrawable(context, resId)
        setImageDrawable(drawable)
    }

    @JvmStatic
    @BindingAdapter("android:progress")
    fun ProgressBar.bindProgress(progress : Int) {
        setProgress(progress)
    }

    /**
     * support-v7 version of [androidx.databinding.adapters.ToolbarBindingAdapter]
     */
    @BindingMethods(
        BindingMethod(
            type = Toolbar::class,
            attribute = "android:onMenuItemClick",
            method = "setOnMenuItemClickListener"
        ),
        BindingMethod(
            type = Toolbar::class,
            attribute = "android:onNavigationClick",
            method = "setNavigationOnClickListener"
        )
    )
    class ToolbarV7BindingAdapter

    private fun tintDrawable(drawable: Drawable?, @ColorInt color: Int): Drawable? {
        if (drawable != null && color != 0) {
            val wrapped = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrapped.mutate(), color)
            return wrapped
        }
        return drawable
    }
}
