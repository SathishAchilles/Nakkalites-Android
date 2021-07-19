package `in`.nakkalites.mediaclient.data

object HttpConstants {
    const val TIMEOUT: Long = 30
    const val BASE_URL_DEV: String = "https://nakkalites-dev.herokuapp.com/api/"
    const val BASE_URL_PROD: String = "https://prod.nakkalites.in/api/"
    const val LOGIN = "v1/users/sign_in"
    const val VIDEO_GROUPS = "v2/video_groups"
    const val WEBSERIES = "v2/web_series"
    const val VIDEO_GROUP_DETAIL = "v2/video_groups/{video-group-id}"
    const val WEBSERIES_DETAIL = "v2/web_series/{webseries-id}"
    const val VIDEO_DETAIL = "v2/videos/{video-id}"
    const val VIDEO_TRACK = "v1/videos/{video-id}/track"
    const val VIDEO_RELATED = "v2/videos/{video-id}/related"
    const val TOKEN_REFRESH = "v1/token/refresh"
    const val FCM_REFRESH = "v1/fcm_token"
    const val USER_PROFILE = "v1/users/profile"
    const val PLANS = "v1/plans"
    const val FAQS = "v1/faqs"
    const val PLAN_VERIFY = "v1/users/payments/verify"
    const val PLAN_FAILURE = "v1/users/payments/failure"
    const val SUBSCRIPTIONS = "v1/users/memberships"
    const val TERMS_CONDITIONS = "https://d29konf8kupzms.cloudfront.net/Nakkalites+Terms+of+Use.pdf"
    const val PRIVACY_POLICY = "https://d29konf8kupzms.cloudfront.net/Privacy+Policy.pdf"
}

object HttpStatus {
    const val SUCCESS = 200
    const val UNAUTHORIZED = 401
    const val LOGOUT = 423
}
