package `in`.nakkalites.mediaclient.view.utils

import `in`.nakkalites.mediaclient.view.video.VideoDetailActivity
import android.content.Context

fun openVideoDetailPage(
    context: Context, id: String, name: String, thumbnail: String, url: String
) {
    context.startActivity(VideoDetailActivity.createIntent(context, id, name, thumbnail, url))
}
