package `in`.nakkalites.mediaclient.view.video

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.ActivityVideoPlayerBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.OrientationManager
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.isRotationEnabled
import `in`.nakkalites.mediaclient.viewmodel.video.VideoPlayerVm
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit


class VideoPlayerActivity : BaseActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    val vm by viewModel<VideoPlayerVm>()
    private val id by lazy {
        intent.getStringExtra(AppConstants.VIDEO_ID)
    }
    private val name by lazy {
        intent.getStringExtra(AppConstants.VIDEO_NAME)
    }
    private val thumbnail by lazy {
        intent.getStringExtra(AppConstants.VIDEO_THUMBNAIL)
    }
    private val url by lazy {
        intent.getStringExtra(AppConstants.VIDEO_URL)
    }
    private val lastPlayedTime: Long? by lazy {
        intent.getLongExtra(AppConstants.LAST_PLAYED_TIME, 0L)
    }
    private val duration: Long? by lazy {
        intent.getLongExtra(AppConstants.DURATION, 0L)
    }

    private val stethoInterceptor by inject<StethoInterceptor>()
    private val bandwidthMeter by inject<DefaultBandwidthMeter>()
    private val trackSelector by inject<MappingTrackSelector>()
    private val loadControl by inject<LoadControl>()
    private val simpleCache by inject<SimpleCache>()
    private lateinit var orientationManager: OrientationManager
    private lateinit var videoObserver: VideoObserver

    companion object {
        @JvmStatic
        fun createIntent(
            ctx: Context, id: String, name: String, thumbnail: String, url: String,
            duration: Long?, lastPayedTime: Long?
        ): Intent = Intent(ctx, VideoPlayerActivity::class.java)
            .putExtra(AppConstants.VIDEO_ID, id)
            .putExtra(AppConstants.VIDEO_NAME, name)
            .putExtra(AppConstants.VIDEO_THUMBNAIL, thumbnail)
            .putExtra(AppConstants.VIDEO_URL, url)
            .putExtra(AppConstants.LAST_PLAYED_TIME, lastPayedTime)
            .putExtra(AppConstants.DURATION, duration)
    }

    private var cookieStore = HashMap<HttpUrl, List<Cookie>>()
    private val cookieJar = object : CookieJar {
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val cookies = cookieStore[url]
            return cookies?.let { it } ?: listOf()
        }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url] = cookies
        }
    }

    private val okClient: OkHttpClient
        get() = OkHttpClient.Builder()
            .connectTimeout(
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS.toLong(), TimeUnit.MILLISECONDS
            )
            .cookieJar(cookieJar)
            .readTimeout(
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS.toLong(), TimeUnit.MILLISECONDS
            )
            .addNetworkInterceptor(stethoInterceptor)
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
//        window.decorView.setOnSystemUiVisibilityChangeListener {
//            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
//            Timber.e("orientation system  ${it or View.SYSTEM_UI_FLAG_FULLSCREEN}")
//            if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
//                var newUiOptions: Int = window.decorView.systemUiVisibility
//                newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_FULLSCREEN
//                window.decorView.systemUiVisibility = newUiOptions
//            } else {
//                window.decorView.systemUiVisibility =
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//            }
//        }
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)
        binding.vm = vm
        vm.setArgs(id, name, thumbnail, url)
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        videoObserver = VideoObserver(
            this, binding.videoPlayerWrapper, id, url, duration ?: 0L, lastPlayedTime ?: 0L,
            vm.name, binding.playerView, vm, bandwidthMeter, trackSelector, simpleCache,
            okClient, loadControl
        )
        lifecycle.addObserver(videoObserver)
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Error -> {
                    errorHandler(it.throwable)
                }
            }
        })
        orientationManager = OrientationManager(this, orientationChangeListener = object :
            OrientationManager.OrientationChangeListener {
            override fun onOrientationChanged(newOrientation: Int) {
                Timber.e("orientation $newOrientation")
                if (newOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    videoObserver.setLandscapeOrientation(binding.videoPlayerWrapper)
                } else {
                    videoObserver.setPortraitOrientation()
                }
            }
        })
        if (contentResolver.isRotationEnabled()) {
            val handler = Handler()
            handler.postDelayed(Runnable {
                orientationManager.enable()
            }, 500)
        }
        videoObserver.setLandscapeOrientation(binding.videoPlayerWrapper)
    }

    override fun onPause() {
        super.onPause()
        vm.uploadVideoProgress()
    }

    override fun onDestroy() {
        orientationManager.disable()
        vm.uploadVideoProgress()
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return super.dispatchKeyEvent(event) || videoObserver.playerView.dispatchKeyEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//        } else {
//            showSystemUI()
        }
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}
