package `in`.nakkalites.mediaclient.view.binding

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

object Bindings {

    @JvmStatic
    @BindingAdapter("android:onClick")
    fun View.bindOnClick(onClickRunnable: Function0<Unit>?) {
        setOnClickListener { onClickRunnable?.invoke() }
    }

    @BindingAdapter("android:visibility")
    fun View.bindVisibility(visibleOrGone: Boolean) {
        visibility = if (visibleOrGone) View.VISIBLE else View.GONE
    }

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
}
