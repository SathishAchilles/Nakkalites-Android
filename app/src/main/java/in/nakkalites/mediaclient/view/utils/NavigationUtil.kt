package `in`.nakkalites.mediaclient.view.utils

import `in`.nakkalites.mediaclient.view.video.VideoDetailActivity
import `in`.nakkalites.mediaclient.view.video.VideoPlayerActivity
import android.content.Context

fun openVideoDetailPage(
    context: Context, id: String, name: String, thumbnail: String, url: String
) {
    context.startActivity(VideoDetailActivity.createIntent(context, id, name, thumbnail, url))
}

fun openVideoPlayerPage(
    context: Context, id: String, name: String, thumbnail: String, url: String
) {
    context.startActivity(VideoPlayerActivity.createIntent(context, id, name, thumbnail, url))
}
