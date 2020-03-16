package `in`.nakkalites.mediaclient.view.video

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.view.utils.playStoreUrl
import `in`.nakkalites.mediaclient.view.utils.setLandScapeOrientation
import `in`.nakkalites.mediaclient.view.utils.setPortraitOrientation
import `in`.nakkalites.mediaclient.view.utils.shareTextIntent
import `in`.nakkalites.mediaclient.viewmodel.utils.toTimeString
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.concurrent.TimeUnit

class VideoObserver(
    private val activity: Activity, private val videoWrapper: FrameLayout, private val id: String,
    url: String, duration: Long, private var lastPlayedTime: Long,
    private val name: String?, val playerView: PlayerView, private val playerTracker: PlayerTracker,
    bandwidthMeter: DefaultBandwidthMeter, private val trackSelector: MappingTrackSelector,
    simpleCache: SimpleCache, private val okHttpClient: OkHttpClient, loadControl: LoadControl
) : LifecycleObserver {

    private val disposables = CompositeDisposable()
    private val MAX_FORWARD_BACKWARD_IN_MS = 10 * 1000 // 10 seconds
    private var videoDuration: Long? = duration
    private val player = ExoPlayerFactory.newSimpleInstance(
        activity, DefaultRenderersFactory(activity), trackSelector, loadControl
    )
    private val mediaDataSourceFactory =
        createDataSourceFactory(activity, simpleCache, bandwidthMeter)
    private val mediaSource = HlsMediaSource.Factory(mediaDataSourceFactory)
        .setAllowChunklessPreparation(true)
        .createMediaSource(Uri.parse(url))
    private val playPauseButton = activity.findViewById<View>(R.id.play_pause)
    private val volumeButton = activity.findViewById<ImageView>(R.id.volume_button)
    private val progressBar = activity.findViewById<SpinKitView>(R.id.progress_bar)
    private val backButton = activity.findViewById<ImageView>(R.id.back)
    private val shareButton = activity.findViewById<ImageView>(R.id.share)
    private val fullscreen = activity.findViewById<ImageView>(R.id.fullscreen_button)
    private val remainingTimeView = activity.findViewById<TextView>(R.id.remaining_duration)
    private var currentSecond: Long = 0
        set(value) {
            field = value
            playerTracker.timeElapsed = field * 1000
        }
    private var remainingTime: Long = 0
        set(value) {
            field = value
            remainingTimeView.text = value.toTimeString(withLiteral = false, includeZeros = true)
        }

    init {
        initializePlayer()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        hideSystemUi()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        playerTracker.shouldPauseCurrentVideo = true
        player.playWhenReady = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        releasePlayer()
        disposables.clear()
    }

    private fun hideSystemUi() {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun initializePlayer() {
        if (BuildConfig.DEBUG) {
            player.addAnalyticsListener(EventLogger(trackSelector))
        }
        backButton.setOnClickListener {
            activity.onBackPressed()
        }
        playPauseButton.setOnClickListener {
            playerTracker.shouldPauseCurrentVideo = player.playWhenReady
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(player.currentWindowIndex, C.TIME_UNSET)
                player.playWhenReady = true
            } else {
                playerView.dispatchMediaKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                )
            }
        }
        shareButton.setOnClickListener {
            val intent = shareTextIntent(
                activity.getString(R.string.share_sheet_title, name),
                activity.getString(R.string.video_share_text, name, activity.playStoreUrl())
            )
            activity.startActivity(intent)
        }
        changeVolumeIcon(player, volumeButton)
        val parent = playerView.parent as FrameLayout
        if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fullscreen.setImageResource(R.drawable.ic_enter_fullscreen)
            videoWrapper.tag = parent
        } else {
            fullscreen.setImageResource(R.drawable.ic_exit_fullscreen)
            videoWrapper.tag = null
        }
        fullscreen.setOnClickListener {
            changeFullscreenButton(parent)
        }
        volumeButton.setOnClickListener {
            player.volume = if (player.volume == 0f) 1f else 0f
            changeVolumeIcon(player, volumeButton)
        }
        with(player) {
            prepare(mediaSource)
            Timber.e("previous ${player.currentWindowIndex} $lastPlayedTime")
            if (lastPlayedTime != 0L) {
                seekTo(player.currentWindowIndex, lastPlayedTime)
                Timber.e("seekTo ${player.currentWindowIndex} $lastPlayedTime")
            }
            playWhenReady = !playerTracker.shouldPauseCurrentVideo
        }
        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.player = player
        playerView.requestFocus()
        disposables += Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .filter {
                (player.playbackState == Player.STATE_READY && player.playWhenReady)
                        || player.playbackState == Player.STATE_ENDED
            }
            .map { player.currentPosition }
            .subscribe {
                currentSecond = it / 1000
                remainingTime = player.duration - player.currentPosition
            }

        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        progressBar.visibility = View.GONE
                        playPauseButton.visibility = View.VISIBLE
                        videoDuration = player.duration
                        remainingTime = player.duration - player.currentPosition
                        playerTracker.duration = videoDuration!!
                    }
                    Player.STATE_BUFFERING -> {
                        progressBar.visibility = View.VISIBLE
                        playPauseButton.visibility = View.GONE
                    }
                    else -> {
                        progressBar.visibility = View.GONE
                    }
                }
            }
        })
        playerView.setRewindIncrementMs(MAX_FORWARD_BACKWARD_IN_MS)
        playerView.setFastForwardIncrementMs(MAX_FORWARD_BACKWARD_IN_MS)
    }

    private fun changeFullscreenButton(parent: FrameLayout) {
        loge("orientation ${activity.requestedOrientation}")
        if (videoWrapper.tag == null) {
            setLandscapeOrientation(parent)
        } else {
            setPortraitOrientation(parent)
        }
    }

    fun setLandscapeOrientation(parent: FrameLayout) {
        fullscreen.setImageResource(R.drawable.ic_exit_fullscreen)
        activity.setLandScapeOrientation()
        videoWrapper.tag = parent
    }

    fun setPortraitOrientation(parent: FrameLayout) {
        if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            || activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            || activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            || activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        ) {
            activity.setPortraitOrientation()
        }
        fullscreen.setImageResource(R.drawable.ic_enter_fullscreen)
        videoWrapper.tag = null
    }

    private fun changeVolumeIcon(player: SimpleExoPlayer, volumeButton: ImageView) {
        val volumeDrawableRes =
            if (player.volume == 0f) R.drawable.ic_volume_off else R.drawable.ic_volume_on
        volumeButton.setImageResource(volumeDrawableRes)
    }

    private fun releasePlayer() {
        player.release()
    }

    private fun createDataSourceFactory(
        activity: Activity, simpleCache: SimpleCache, bandwidthMeter: DefaultBandwidthMeter
    ): DataSource.Factory {
        val userAgent = Util.getUserAgent(activity, activity.getString(R.string.app_name))
        val dataSourceFactory = DefaultDataSourceFactory(
            activity, bandwidthMeter,
            OkHttpDataSourceFactory(okHttpClient, userAgent, bandwidthMeter)
        )
        return CacheDataSourceFactory(simpleCache, dataSourceFactory)
    }
}
