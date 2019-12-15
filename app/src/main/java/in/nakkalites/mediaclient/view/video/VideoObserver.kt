package `in`.nakkalites.mediaclient.view.video

import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.R
import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
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
import java.util.concurrent.TimeUnit

//TODO - On orientation change prevent restart of the video
class VideoObserver(
    private val activity: Activity, private val simpleCache: SimpleCache, private val url: String,
    private val playerView: PlayerView, private val okHttpClient: OkHttpClient,
    private val playerTracker: PlayerTracker
) : LifecycleObserver {

    private val disposables = CompositeDisposable()
    private lateinit var player: SimpleExoPlayer
    lateinit var mediaDataSourceFactory: DataSource.Factory
    private var videoDuration: Long? = null
    private var currentSecond: Long = 0

    init {
        initializePlayer()
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

    private fun initializePlayer() {
        val bandwidthMeter = DefaultBandwidthMeter.Builder(activity)
            .build()
        val trackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory())
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(6000, 18000, 500, 3000)
            .createDefaultLoadControl()
        player = ExoPlayerFactory.newSimpleInstance(
            activity, DefaultRenderersFactory(activity), trackSelector, loadControl
        )
        mediaDataSourceFactory = createDataSourceFactory(activity, simpleCache, bandwidthMeter)
        val mediaSource = HlsMediaSource.Factory(mediaDataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(Uri.parse(url))

        if (BuildConfig.DEBUG) {
            player.addAnalyticsListener(EventLogger(trackSelector))
        }
        val playPauseButton = activity.findViewById<View>(R.id.play_pause)
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
        val volumeButton = activity.findViewById<ImageView>(R.id.volume_button)
        changeVolumeIcon(player, volumeButton)
        volumeButton.setOnClickListener {
            player.volume = if (player.volume == 0f) 1f else 0f
            changeVolumeIcon(player, volumeButton)
        }

        with(player) {
            prepare(mediaSource)
            playWhenReady = !playerTracker.shouldPauseCurrentVideo
        }

        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.player = player
        playerView.requestFocus()
        disposables += Observable.interval(1, TimeUnit.SECONDS)
            .filter {
                (player.playbackState == Player.STATE_READY && player.playWhenReady)
                        || player.playbackState == Player.STATE_ENDED
            }
            .map { player.currentPosition }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { currentSecond = it / 1000 }
        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    videoDuration = player.duration / 1000
                    playerTracker.duration = videoDuration!!
                }
            }
        })
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
            activity,
            bandwidthMeter,
            OkHttpDataSourceFactory(okHttpClient, userAgent, bandwidthMeter)
        )
        return CacheDataSourceFactory(simpleCache, dataSourceFactory)
    }
}
