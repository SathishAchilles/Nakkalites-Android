package `in`.nakkalites.mediaclient.view.video

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.ActivityVideoPlayerBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.viewmodel.video.VideoPlayerVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
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
    private val bandwidthMeter by inject<DefaultBandwidthMeter>()
    private val trackSelector by inject<MappingTrackSelector>()
    private val loadControl by inject<LoadControl>()
    private val simpleCache by inject<SimpleCache>()
    private lateinit var videoObserver: VideoObserver

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

    private val okClient: OkHttpClient
        get() = OkHttpClient.Builder()
            .connectTimeout(
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS.toLong(), TimeUnit.MILLISECONDS
            )
            .readTimeout(
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS.toLong(), TimeUnit.MILLISECONDS
            )
            .addInterceptor(stethoInterceptor)
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)
        binding.vm = vm
        vm.setArgs(id, name, thumbnail, url)
        videoObserver = VideoObserver(
            this, id, url, binding.playerView, vm, bandwidthMeter, trackSelector, simpleCache,
            okClient, loadControl
        )
        lifecycle.addObserver(videoObserver)
    }

}
