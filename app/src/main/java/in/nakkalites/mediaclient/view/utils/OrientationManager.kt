package `in`.nakkalites.mediaclient.view.utils

import android.content.Context
import android.content.res.Configuration
import android.view.OrientationEventListener

class OrientationManager constructor(
    private val context: Context, private val orientationChangeListener: OrientationChangeListener
) :
    OrientationEventListener(context) {
    private var previousAngle: Int = 0
    var orientation: Int = 0

    override fun onOrientationChanged(orientation: Int) {
        if (orientation == -1) return
        if (this.orientation == 0) {
            this.orientation = context.resources.configuration.orientation
            orientationChangeListener.onOrientationChanged(this.orientation)
        }
        if (this.orientation == Configuration.ORIENTATION_LANDSCAPE
            && (previousAngle > 10 && orientation <= 10
                    || (previousAngle in 271..349 && orientation >= 350))
        ) {
            orientationChangeListener.onOrientationChanged(Configuration.ORIENTATION_PORTRAIT)
            this.orientation = Configuration.ORIENTATION_PORTRAIT
        }

        if (this.orientation == Configuration.ORIENTATION_PORTRAIT && ((previousAngle < 90
                    && orientation >= 90
                    && orientation < 270) || (previousAngle > 280
                    && orientation <= 280
                    && orientation > 180))
        ) {
            orientationChangeListener.onOrientationChanged(Configuration.ORIENTATION_LANDSCAPE)
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        }
        previousAngle = orientation
    }

    interface OrientationChangeListener {
        fun onOrientationChanged(newOrientation: Int)
    }
}
