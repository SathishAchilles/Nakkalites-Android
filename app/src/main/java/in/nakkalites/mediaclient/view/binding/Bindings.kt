package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.view.binding.ViewModelBinder.Companion.defaultBinder
import `in`.nakkalites.mediaclient.view.utils.DebounceOnClickListener
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import `in`.nakkalites.mediaclient.viewmodel.utils.StyleFormatText
import `in`.nakkalites.mediaclient.viewmodel.utils.toSpannable
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.SuperscriptSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.*
import androidx.annotation.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.TintableBackgroundView
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import org.jetbrains.annotations.NotNull
import timber.log.Timber


object Bindings {
    @JvmStatic
    @BindingAdapter("android:onClick")
    fun View.bindOnClick(onClickRunnable: Runnable?) {
        setOnClickListener { onClickRunnable?.run() }
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
        lp.marginStart = context.resources.getDimension(marginStart).toInt()
        layoutParams = lp
    }

    @JvmStatic
    @BindingAdapter("android:layout_marginEnd")
    fun View.bindMarginEnd(@DimenRes marginEnd: Int) {
        val lp = layoutParams as MarginLayoutParams
        lp.marginEnd = context.resources.getDimension(marginEnd).toInt()
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
    fun ProgressBar.bindProgress(progress: Int) {
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

    @JvmStatic
    @BindingAdapter(value = ["entries", "enabled", "onItemClick"], requireAll = false)
    fun AppCompatSpinner.bindAdapter(
            items: List<Pair<String, String>>, enabled: Boolean,
            onItemClick: (@NotNull Pair<String, String>) -> Unit
    ) {
        val adapter = ArrayAdapter<String>(
                this.context, R.layout.spinner_item_selected, items.map { it.second })
        adapter.setDropDownViewResource(R.layout.spinner_drop_down_item)
        isEnabled = enabled
        isClickable = enabled
        setAdapter(adapter)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                onItemClick.invoke(items[position])
            }
        }
    }

    private fun tintDrawable(drawable: Drawable?, @ColorInt color: Int): Drawable? {
        if (drawable != null && color != 0) {
            val wrapped = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrapped.mutate(), color)
            return wrapped
        }
        return drawable
    }

    @Suppress("DEPRECATION")
    @JvmStatic
    @BindingAdapter(value = ["appendStarSuperScript"], requireAll = false)
    fun TextView.bindStarSuperScriptText(
            isAppend: Boolean?
    ) {
        val value = text?.toString()
        if (isAppend != null && isAppend && value != null) {
            includeFontPadding = true
            isAllCaps = false
            val starSuperScript = context.getString(R.string.star_superscript)
            val spannable = SpannableStringBuilder("$value $starSuperScript")
            spannable.setSpan(
                    SuperscriptSpan(),
                    spannable.length - 1,
                    spannable.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#00559e")),
                    spannable.length - 1, spannable.length, 0
            )
            text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(value + starSuperScript, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(value + starSuperScript)
            }
//            text = spannable
        }
    }

    /** Data-binding's automatic setter doesn't work for &lt;T extends ListAdapter &amp; Filterable&gt;.  */
    @JvmStatic
    @BindingAdapter(value = ["adapter"])
    fun <T> setAutoCompleteTextViewAdapter(
            view: AppCompatAutoCompleteTextView, @Nullable adapter: ArrayAdapter<T>
    ) {
        view.setAdapter(adapter)
    }

    @JvmStatic
    @BindingAdapter("suggestions")
    fun setSuggestions(
            view: AppCompatAutoCompleteTextView,
            @Nullable suggestions: Collection<DisplayText?>?
    ) {
        @LayoutRes val layoutId: Int = R.layout.simple_spinner_dropdown
        @IdRes val textViewId: Int = R.id.text
        if (suggestions != null) {
            val list = suggestions.map { res -> res?.getText(view.resources) }
            val arrayAdapter: ArrayAdapter<String?> = ArrayAdapter<String?>(
                    view.context, layoutId, textViewId, ArrayList(list)
            )
            view.setAdapter(arrayAdapter)
        } else {
            val emptyAdapter: ArrayAdapter<String?> = ArrayAdapter<String?>(
                    view.context, layoutId, textViewId, ArrayList()
            )
            view.setAdapter(emptyAdapter)
        }
        view.threshold = 0
    }

    @JvmStatic
    @BindingAdapter(
            value = ["entries", "entryLayout", "entryViewProvider", "entryBinder", "shouldAppendEntries"],
            requireAll = false
    )
    fun <T : BaseViewModel> bindViewGroupEntries(
            viewGroup: ViewGroup, entries: List<T>?, entryLayout: Int,
            entryProvider: ViewProvider?, entryBinder: ViewModelBinder?,
            shouldAppendEntries: Boolean
    ) {
        require(!(entryLayout <= 0 && entryProvider == null))
        if (entries == null) return
        val inflater = LayoutInflater.from(viewGroup.context)
        if (shouldAppendEntries) {
            val entriesToAppend = entries.subList(viewGroup.childCount, entries.size)
            bindEntries(
                    viewGroup,
                    entriesToAppend,
                    entryLayout,
                    entryProvider,
                    entryBinder,
                    inflater
            )
        } else {
            viewGroup.removeAllViews()
            bindEntries(viewGroup, entries, entryLayout, entryProvider, entryBinder, inflater)
        }
    }

    private fun <T : BaseViewModel> bindEntries(
            viewGroup: ViewGroup, entries: List<T>, entryLayout: Int,
            entryProvider: ViewProvider?, entryBinder: ViewModelBinder?,
            inflater: LayoutInflater
    ) {
        for (entry in entries) {
            val layoutId = entryProvider?.getView(entry) ?: entryLayout
            val binding = DataBindingUtil
                    .inflate<ViewDataBinding>(inflater, layoutId, viewGroup, false)
            defaultBinder.bind(binding, entry)
            entryBinder?.bind(binding, entry)
            binding.executePendingBindings()
            viewGroup.addView(binding.root)
        }
    }

    @JvmStatic
    @BindingAdapter("debounceOnClick")
    fun View.bindDebounceOnClick(debounceOnClickRunnable: Runnable?) {
        Timber.e("$id bindDebounceOnClick $debounceOnClickRunnable")
        if (debounceOnClickRunnable == null) return
        setOnClickListener(object : DebounceOnClickListener() {
            override fun debouncedOnClick(v: View) {
                Timber.e("$id bindDebounceOnClick")
                debounceOnClickRunnable.run()
            }
        })
    }

    @JvmStatic
    @BindingAdapter("android:backgroundTint")
    fun View.bindBackgroundTint(@ColorInt color: Int) {
        if (this is TintableBackgroundView) {
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(color))
        } else {
            background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    @JvmStatic
    @BindingAdapter("android:text")
    fun TextView.bindStyleFormatText(styleFormatText: StyleFormatText?) {
        text = styleFormatText?.toSpannable(context)
    }
}
