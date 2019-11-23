package `in`.nakkalites.mediaclient.app

import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient

object StethoHelper {

    fun injectStethoIfDebug(context: Context, okHttpClientBuilder: OkHttpClient.Builder)
            : OkHttpClient.Builder {
        Stetho.initializeWithDefaults(context)
        return okHttpClientBuilder.addNetworkInterceptor(StethoInterceptor())
    }
}
