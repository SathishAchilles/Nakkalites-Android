package `in`.nakkalites.mediaclient.view.utils

import `in`.nakkalites.mediaclient.view.subscription.SubscriptionsActivity
import `in`.nakkalites.mediaclient.view.video.VideoDetailActivity
import `in`.nakkalites.mediaclient.view.video.VideoPlayerActivity
import android.content.Context

fun openVideoDetailPage(
    context: Context, id: String, name: String, thumbnail: String, url: String?
) {
    context.startActivity(VideoDetailActivity.createIntent(context, id, name, thumbnail, url))
}

fun openVideoPlayerPage(
    context: Context, id: String, name: String, thumbnail: String, url: String,
    duration: Long? = 0L, lastPayedTime: Long? = 0L, adTimes: List<Long> = listOf(),
    showAds: Boolean, shouldPlay: Boolean, planUid: String
) {
    if(shouldPlay) {
        context.startActivity(
            VideoPlayerActivity.createIntent(
                context, id, name, thumbnail, url, duration, lastPayedTime, adTimes, showAds
            )
        )
    } else {
        context.startActivity(SubscriptionsActivity.createIntent(context, name, thumbnail, planUid))
    }
}
