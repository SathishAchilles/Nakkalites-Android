package `in`.nakkalites.mediaclient.view.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ViewAnimator
import androidx.databinding.ViewDataBinding

class ViewAnimator : ViewAnimator {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun setDisplayedChild(whichChild: Int) {
        visibility = VISIBLE
        if (displayedChild != whichChild) {
            super.setDisplayedChild(whichChild)
        } else {
            // no-op
        }
    }

    fun setDisplayedChild(view: View) {
        var i = 0
        val count = childCount
        while (i < count) {
            if (getChildAt(i).id == view.id) {
                displayedChild = i
                return
            }
            i++
        }
    }

    fun setDisplayedChild(viewDataBinding: ViewDataBinding) {
        setDisplayedChild(viewDataBinding.root)
    }

    // This is to expose `child` as a convenient Kotlin property.
    var child: View
        get() = getChildAt(displayedChild)
        set(view) {
            setDisplayedChild(view)
        }

}
