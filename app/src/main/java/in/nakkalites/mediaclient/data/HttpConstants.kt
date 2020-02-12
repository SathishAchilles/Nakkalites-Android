package `in`.nakkalites.mediaclient.data

object HttpConstants {
    const val TIMEOUT: Long = 30
    const val BASE_URL_DEV: String = "https://sleepy-lowlands-51142.herokuapp.com/api/"
    const val BASE_URL_PROD: String = "https://www.nakkalites.com/api/"
    const val LOGIN = "v1/users/sign_in"
    const val VIDEO_GROUPS = "v1/video_groups"
    const val WEBSERIES = "v1/web_series"
    const val VIDEO_GROUP_DETAIL = "v1/video_groups/{video-group-id}"
    const val WEBSERIES_DETAIL = "v1/web_series/{webseries-id}"
    const val VIDEO_DETAIL = "v1/videos/{video-id}"
    const val VIDEO_TRACK = "v1/videos/{video-id}/track"
    const val VIDEO_RELATED = "v1/videos/{video-id}/related"
    const val TERMS_CONDITIONS = "https://www.nakkalites.com/api/terms-conditions"
    const val PRIVACY_POLICY = "https://www.nakkalites.com/api/privacy"
}

object HttpStatus {
    const val SUCCESS = 200
    const val UNAUTHORIZED = 401
    const val LOGOUT = 462
}
