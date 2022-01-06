package `in`.nakkalites.mediaclient.app.utils


import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import android.app.Application
import android.os.Build
import android.os.Bundle
import com.google.android.gms.security.ProviderInstaller
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.internal.immutableListOf
import timber.log.Timber
import java.security.KeyStore
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Enables TLS v1.2 when creating SSLSockets.
 * For some reason, Android supports TLS v1.2 from API 16, but enables it by default only from API 20.
 *
 * See https://guides.codepath.com/android/Using-OkHttp#enabling-tls-v1-2-on-older-devices.
 */
object Tls12SocketUtil {

    @JvmStatic
    fun enable(application: Application): Pair<Boolean, String?> {
        return try {
            enableForHttpsUrlConnection()
            val startTime = System.currentTimeMillis()
            ProviderInstaller.installIfNeeded(application)
            val endTime = System.currentTimeMillis()
            Timber.d("Time taken to install provider: ${endTime - startTime}")
            val sslContext = SSLContext.getInstance(TLS_V12_PROTOCOL)
            sslContext.init(null, null, null)
            Pair(true, "")
        } catch (e: Exception) {
            Timber.e(e)
            Pair(false, e.message)
        }
    }

    private fun enableForHttpsUrlConnection() {
        try {
            val sslContext = SSLContext.getInstance(TLS_V12_PROTOCOL)
            val tls12SocketFactory = Tls12SocketFactory(sslContext.socketFactory)
            HttpsURLConnection.setDefaultSSLSocketFactory(tls12SocketFactory)
            Timber.d("$TLS_V12_PROTOCOL enabled for HttpsUrlConnection.")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun trackProviderInstallFailed(error: String?, analyticsManager: AnalyticsManager) {
        val bundle =
            Bundle().apply { putString(AnalyticsConstants.Property.OPENSSL_ERROR_MESSAGE, error) }
        analyticsManager.logEvent(AnalyticsConstants.Event.OPENSSL_INSTALL_FAILED, bundle)
    }
}

private const val TLS_V12_PROTOCOL = "TLSv1.2"