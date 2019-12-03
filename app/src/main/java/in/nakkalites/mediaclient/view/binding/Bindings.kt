package `in`.nakkalites.mediaclient.view.binding

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.ImageView
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
    }

    @JvmStatic
    @BindingAdapter("bringToFront")
    fun View.bindBringToFront(bringToFront: Boolean) {
        if (bringToFront) bringToFront()
    }

    @BindingMethods(
        BindingMethod(
            type = TabLayout::class,
            attribute = "onTabSelect",
            method = "addOnTabSelectedListener"
        )
    )
    class TabLayoutBindingAdapter

}
