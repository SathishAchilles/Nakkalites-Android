package `in`.nakkalites.mediaclient.view.video

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.ActivityVideoPlayerBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.utils.StethoInterceptorFactory
import `in`.nakkalites.mediaclient.viewmodel.video.VideoPlayerVm
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import okhttp3.OkHttpClient
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
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
    private val stethoInterceptor by inject<StethoInterceptor>()
    private val simpleCache by inject<SimpleCache>()
    private lateinit var player: SimpleExoPlayer
    private lateinit var mediaDataSourceFactory: DataSource.Factory

    companion object {
        @JvmStatic
        fun createIntent(
            ctx: Context, id: String, name: String, thumbnail: String, url: String
        ): Intent =
            Intent(ctx, VideoPlayerActivity::class.java)
                .putExtra(AppConstants.VIDEO_ID, id)
                .putExtra(AppConstants.VIDEO_NAME, name)
                .putExtra(AppConstants.VIDEO_THUMBNAIL, thumbnail)
                .putExtra(AppConstants.VIDEO_URL, url)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)
        binding.vm = vm
        vm.setArgs(id, name, thumbnail, url)
    }

    private fun initializePlayer() {
        val bandwidthMeter = DefaultBandwidthMeter.Builder(this)
            .build()
        val trackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(6000, 18000, 500, 3000)
            .createDefaultLoadControl()
        player =
            ExoPlayerFactory.newSimpleInstance(
                this,
                DefaultRenderersFactory(this),
                trackSelector,
                loadControl
            )
        mediaDataSourceFactory = createDataSourceFactory(this, simpleCache, bandwidthMeter)
        val mediaSource = HlsMediaSource.Factory(mediaDataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(Uri.parse(vm.url))

        with(player) {
            prepare(mediaSource, false, false)
            playWhenReady = true
        }


        binding.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        binding.playerView.player = player
        binding.playerView.requestFocus()

    }

    private fun releasePlayer() {
        player.release()
    }

    public override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) initializePlayer()
    }

    public override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23) initializePlayer()
    }

    public override fun onPause() {
        super.onPause()

        if (Util.SDK_INT <= 23) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) releasePlayer()
    }


    private val okClient: OkHttpClient
        get() = OkHttpClient.Builder()
            .connectTimeout(
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS.toLong(),
                TimeUnit.MILLISECONDS
            )
            .readTimeout(
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS.toLong(),
                TimeUnit.MILLISECONDS
            )
            .addInterceptor(stethoInterceptor)
            .build()

    fun createDataSourceFactory(
        activity: Activity, simpleCache: SimpleCache, bandwidthMeter: DefaultBandwidthMeter? = null
    ): DataSource.Factory {
        val userAgent = Util.getUserAgent(activity, activity.getString(R.string.app_name))
        val dataSourceFactory = DefaultDataSourceFactory(
            activity, bandwidthMeter, OkHttpDataSourceFactory(okClient, userAgent, bandwidthMeter)
        )
        return CacheDataSourceFactory(simpleCache, dataSourceFactory)
    }
}
