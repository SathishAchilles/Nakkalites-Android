package `in`.nakkalites.mediaclient.view.video

import `in`.nakkalites.mediaclient.BuildConfig
import `in`.nakkalites.mediaclient.R
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
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
import java.util.concurrent.TimeUnit

class VideoObserver(
    private val activity: Activity, private val id: String, url: String,
    private val playerView: PlayerView, private val playerTracker: PlayerTracker,
    bandwidthMeter: DefaultBandwidthMeter, private val trackSelector: MappingTrackSelector,
    simpleCache: SimpleCache, private val okHttpClient: OkHttpClient, loadControl: LoadControl
) : LifecycleObserver {

    private val disposables = CompositeDisposable()
    private var videoDuration: Long? = null
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
    private var currentSecond: Long = 0
        set(value) {
            field = value
            videoDuration?.let {
                playerTracker.trackVideoProgress(currentSecond)
            }
        }

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
            .observeOn(AndroidSchedulers.mainThread())
            .filter {
                (player.playbackState == Player.STATE_READY && player.playWhenReady)
                        || player.playbackState == Player.STATE_ENDED
            }
            .map { player.currentPosition }
            .subscribe { currentSecond = it / 1000 }
        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    progressBar.visibility = View.GONE
                    playPauseButton.visibility = View.VISIBLE
                    videoDuration = player.duration / 1000
                    playerTracker.duration = videoDuration!!
                } else if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.visibility = View.VISIBLE
                    playPauseButton.visibility = View.GONE
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
            activity, bandwidthMeter,
            OkHttpDataSourceFactory(okHttpClient, userAgent, bandwidthMeter)
        )
        return CacheDataSourceFactory(simpleCache, dataSourceFactory)
    }
}
