package `in`.nakkalites.mediaclient.view.utils

import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.view.subscription.SubscriptionsActivity
import `in`.nakkalites.mediaclient.view.video.VideoDetailActivity
import `in`.nakkalites.mediaclient.view.video.VideoPlayerActivity
import android.content.Context
import android.os.Bundle

fun openVideoDetailPage(
    context: Context, id: String, name: String, thumbnail: String, url: String?
) {
    context.startActivity(VideoDetailActivity.createIntent(context, id, name, thumbnail, url))
}

fun openVideoPlayerPage(
    context: Context,
    analyticsManager: AnalyticsManager,
    id: String,
    name: String,
    thumbnail: String,
    url: String,
    duration: Long? = 0L,
    lastPayedTime: Long? = 0L,
    adTimes: List<Long> = listOf(),
    showAds: Boolean,
    shouldPlay: Boolean,
    planUid: String?,
    planName: String?
) {
    trackVideoClicked(analyticsManager, planName, shouldPlay, showAds)
    if (shouldPlay) {
        context.startActivity(
            VideoPlayerActivity.createIntent(
                context, id, name, thumbnail, url, duration, lastPayedTime, adTimes, showAds
            )
        )
    } else {
        context.startActivity(SubscriptionsActivity.createIntent(context, name, thumbnail, planUid))
    }
}

private fun trackVideoClicked(
    analyticsManager: AnalyticsManager,
    planName: String?,
    shouldPlay: Boolean,
    showAds: Boolean
) {
    val bundle = Bundle().apply {
        putString(AnalyticsConstants.Property.AVAILABLE_PLAN, planName)
        putBoolean(AnalyticsConstants.Property.IS_PLAYABLE, shouldPlay)
        putBoolean(AnalyticsConstants.Property.SHOW_ADS, showAds)
    }
    analyticsManager.logEvent(AnalyticsConstants.Event.VIDEO_CLICKED, bundle)
}
