package `in`.nakkalites.mediaclient.view.utils

import `in`.nakkalites.mediaclient.view.video.VideoDetailActivity
import android.content.Context

object NavigationUtil {

    fun openVideoDetailPage(context: Context, id: String, name: String, thumbnail: String) {
        context.startActivity(VideoDetailActivity.createIntent(context, id, name, thumbnail))
    }
}
