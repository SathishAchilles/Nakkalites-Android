package `in`.nakkalites.mediaclient.data

object HttpConstants {
    const val TIMEOUT: Long = 30
    const val BASE_URL_DEV: String = "https://nakkalites.free.beeceptor.com/api/"
    //    const val BASE_URL_DEV: String = "https://17f8865f-3350-4366-9f8e-b75ed300ce19.mock.pstmn.io/"
    const val BASE_URL_PROD: String = "https://www.nakkalites.com/api/"
    const val LOGIN = "v1/login"
    const val VIDEO_GROUPS = "v1/video_groups"
    const val WEBSERIES = "v1/webseries"
    const val VIDEO_GROUP_DETAIL = "v1/video_group/{video-group-id}"
    const val WEBSERIES_DETAIL = "v1/webseries/{webseries-id}"
    const val VIDEO_DETAIL = "v1/video/{video-id}"
}
