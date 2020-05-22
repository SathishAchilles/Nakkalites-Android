package `in`.nakkalites.mediaclient.app.manager

import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants.Property
import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsManager(private val firebaseAnalytics: FirebaseAnalytics) {
    private val defaultProperties: Map<String, String?> = mapOf(
        Property.APP_VERSION_CODE to BuildConfig.VERSION_CODE.toString(),
        Property.APP_VERSION_NAME to BuildConfig.VERSION_NAME,
        Property.BRAND to Build.BRAND,
        Property.MANUFACTURER to Build.MANUFACTURER,
        Property.MODEL to Build.MODEL,
        Property.OS to "Android",
        Property.OS_VERSION to Build.VERSION.RELEASE
    )
    private val defaultPropertiesBundle = Bundle().also { bundle ->
        defaultProperties.map {
            bundle.putString(it.key, it.value)
        }
    }

    fun logEvent(eventName: String, properties: Bundle = Bundle()) {
        properties.putAll(defaultPropertiesBundle)
        firebaseAnalytics.logEvent(eventName, properties)
    }

    fun logUserProperty(key: String, value: String?) {
        value?.let { firebaseAnalytics.setUserProperty(key, value) }
    }

    fun setUserId(userId: String?) {
        userId?.let { firebaseAnalytics.setUserId(it) }
    }

    fun logDefaultProperties() {
        defaultProperties.forEach { logUserProperty(it.key, it.value) }
    }
}
