package `in`.nakkalites.mediaclient.view.utils

import android.view.View
import com.kizitonwose.time.Interval
import com.kizitonwose.time.Millisecond
import com.kizitonwose.time.milliseconds

abstract class DebounceOnClickListener : View.OnClickListener {

    private var lastClickTimeStamp: Interval<Millisecond>? = null

    override fun onClick(v: View) {
        val previousClickTimeStamp = lastClickTimeStamp
        val currentTimeStamp = System.currentTimeMillis().milliseconds
        lastClickTimeStamp = currentTimeStamp
        if (previousClickTimeStamp == null
            || (currentTimeStamp - previousClickTimeStamp) > 400.milliseconds) {
            debouncedOnClick(v)
        }
    }

    abstract fun debouncedOnClick(v: View)
}

