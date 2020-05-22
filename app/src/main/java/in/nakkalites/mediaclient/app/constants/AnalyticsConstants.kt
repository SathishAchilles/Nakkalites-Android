package `in`.nakkalites.mediaclient.app.constants

object AnalyticsConstants {
    object Event {
        const val APP_OPENED = "app_opened"
        const val LOGGED_IN = "logged_in"
        const val WEBSERIES_PAGE_OPENED = "webseries_page_opened"
        const val WEBSERIES_SHARE_CLICKED = "webseries_share_clicked"
        const val WEBSERIES_PLAY_CTA_CLICKED = "webseries_play_cta_clicked"
        const val WEBSERIES_EPISODE_CLICKED = "webseries_episode_clicked"
        const val WEBSERIES_SEASONS_CLICKED = "webseries_seasons_clicked"
        const val VIDEO_GROUP_LIST_PAGE_OPENED = "video_group_list_page_opened"
        const val VIDEO_GROUP_LIST_VIDEO_CLICKED = "video_group_list_video_clicked"
        const val VIDEO_DETAIL_PAGE_OPENED = "video_detail_page_opened"
        const val VIDEO_DETAIL_VIDEO_CLICKED = "video_detail_video_clicked"
        const val VIDEO_DETAIL_PLAY_CTA_CLICKED = "video_detail_play_cta_clicked"
        const val VIDEO_DETAIL_SHARE_CTA_CLICKED = "video_detail_share_cta_clicked"
        const val VIDEO_PLAYER_PAGE_OPENED = "video_player_page_opened"
        const val VIDEO_PAUSED = "video_paused"
        const val HOME_TAB_OPENED = "home_tab_opened"
        const val WEBSERIES_TAB_OPENED = "webseries_tab_opened"
        const val HOME_VIDEO_GROUP_CLICKED = "home_video_group_clicked"
        const val HOME_VIDEO_CLICKED = "home_video_clicked"
        const val BANNER_CLICKED = "banner_clicked"
        const val EMAIL_CLICKED = "email_clicked"
        const val WEBSERIES_CLICKED = "webseries_clicked"
    }

    object Property {
        const val LAST_APP_OPENED = "last_app_opened"
        const val EMAIL = "email"
        const val USER_ID = "user_id"
        const val NAME = "name"
        const val IMAGE_URL = "image_url"
        const val WEBSERIES_ID = "webseries_id"
        const val WEBSERIES_NAME = "webseries_name"
        const val SEASON_NAME = "season_name"
        const val SEASON_ID = "season_id"
        const val VIDEO_GROUP_ID = "video_group_id"
        const val VIDEO_GROUP_NAME = "video_group_name"
        const val VIDEO_ID = "video_id"
        const val VIDEO_NAME = "video_name"
        const val VIDEO_DURATION = "video_duration"
        const val VIDEO_TIME_ELAPSED = "video_time_elapsed"
        const val BANNER_ID = "banner_id"
        const val BANNER_NAME = "banner_name"
        const val BANNER_TYPE = "banner_type"
    }
}
