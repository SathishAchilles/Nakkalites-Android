package `in`.nakkalites.mediaclient.view.utils

import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor

object StethoInterceptorFactory {
    private val interceptor: StethoInterceptor by lazy { StethoInterceptor() }

    fun get(context: Context): StethoInterceptor {
        Stetho.initializeWithDefaults(context)
        return interceptor
    }
}
