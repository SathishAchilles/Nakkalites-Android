package `in`.nakkalites.mediaclient.app

import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient

object StethoHelper {

    // no-op
    internal fun injectStethoIfDebug(ctx: Context, okHttpClientBuilder: OkHttpClient.Builder) =
        okHttpClientBuilder
}
