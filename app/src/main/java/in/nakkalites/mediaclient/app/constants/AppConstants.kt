package `in`.nakkalites.mediaclient.app.constants

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object AppConstants {
    const val USER_EMAIL: String = "user_email"
    const val VIDEO_CACHE_DIRECTORY: String = "media"
    const val PICASSO_CACHE = "picasso-cache"
    const val MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024L // 5MB
    const val MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024L // 50MB
    const val PAGE_SIZE = 20

    const val VIDEO_GROUP_ID = "VIDEO_GROUP_ID"
    const val VIDEO_GROUP_NAME = "VIDEO_GROUP_NAME"
    const val VIDEO_GROUP_CATEGORY = "VIDEO_GROUP_CATEGORY"
    const val WEBSERIES_ID = "WEBSERIES_ID"
    const val WEBSERIES_NAME = "WEBSERIES_NAME"
    const val WEBSERIES_THUMBNAIL = "WEBSERIES_THUMBNAIL"
    const val VIDEO_ID = "VIDEO_ID"
    const val VIDEO_NAME = "VIDEO_NAME"
    const val VIDEO_THUMBNAIL = "VIDEO_THUMBNAIL"
    const val DURATION = "DURATION"
    const val AD_TIMES = "AD_TIMES"
    const val LAST_PLAYED_TIME = "LAST_PLAYED_TIME"
    const val VIDEO_URL = "VIDEO_URL"
    const val WEB_VIEW_URL = "WEB_VIEW_URL"
    const val WEB_VIEW_TITLE = "WEB_VIEW_TITLE"
    const val BOTTOM_NAV_TAB = "BOTTOM_NAV_TAB"
    const val COUNTRY_CODE = "COUNTRY_CODE"
    const val PHONE_NUMBER = "PHONE_NUMBER"

    const val PLAY_STORE_URL = "market://details?id="
    const val CONTACT_EMAIL = "contact@nakkalites.in"
    const val PLAY_STORE_COMPLETE_URL = "https://play.google.com/store/apps/details?id=in.nakkalites.mediaclient"

    object AppCountry {
        const val NAME = "India"
        const val ISO_CODE = "in"
        const val DIALING_CODE = "+91"
        const val DIALING_CODE_WITHOUT_PLUS = "91"
        const val FLAG_EMOJI = "🇮🇳"
        @JvmField val STATES: List<String> = listOf(
            "Andhra Pradesh",
            "Arunachal Pradesh",
            "Assam",
            "Bihar",
            "Chhattisgarh",
            "Goa",
            "Gujarat",
            "Haryana",
            "Himachal Pradesh",
            "Jammu & Kashmir",
            "Jharkhand",
            "Karnataka",
            "Kerala",
            "Madhya Pradesh",
            "Maharashtra",
            "Manipur",
            "Meghalaya",
            "Mizoram",
            "Nagaland",
            "Odisha",
            "Punjab",
            "Rajasthan",
            "Sikkim",
            "Tamil Nadu",
            "Telangana",
            "Tripura",
            "Uttarakhand",
            "Uttar Pradesh",
            "West Bengal",
            "Andaman and Nicobar Islands",
            "Chandigarh",
            "Dadra and Nagar Haveli",
            "Daman & Diu",
            "Delhi",
            "Lakshadweep",
            "Puducherry",
            "Others"
        )
    }
}
