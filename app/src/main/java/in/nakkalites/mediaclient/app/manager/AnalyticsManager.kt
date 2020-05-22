package `in`.nakkalites.mediaclient.app.manager

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsManager(private val firebaseAnalytics: FirebaseAnalytics) {
    fun logEvent(eventName: String, properties: Bundle = Bundle()) {
        firebaseAnalytics.logEvent(eventName, properties)
    }

    fun logUserProperty(key: String, value: String?) {
        value?.let { firebaseAnalytics.setUserProperty(key, value) }
    }

    fun setUserId(userId: String?) {
        userId?.let { firebaseAnalytics.setUserId(it) }
    }
}
