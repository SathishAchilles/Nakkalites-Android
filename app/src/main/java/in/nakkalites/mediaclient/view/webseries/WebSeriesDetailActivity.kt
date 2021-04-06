package `in`.nakkalites.mediaclient.view.webseries

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.ActivityWebSeriesDetailBinding
import `in`.nakkalites.mediaclient.databinding.ItemSeasonEpisodeBinding
import `in`.nakkalites.mediaclient.databinding.ItemSeasonHeaderBinding
import `in`.nakkalites.mediaclient.databinding.ItemWebSeriesDetailBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.RecyclerViewAdapter
import `in`.nakkalites.mediaclient.view.binding.ViewProviders
import `in`.nakkalites.mediaclient.view.binding.viewModelBinder
import `in`.nakkalites.mediaclient.view.binding.viewProvider
import `in`.nakkalites.mediaclient.view.utils.*
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.SeasonEpisodeItemVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.SeasonHeaderVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesDetailItemVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesDetailVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class WebSeriesDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityWebSeriesDetailBinding
    val vm by viewModel<WebSeriesDetailVm>()
    val analyticsManager by inject<AnalyticsManager>()
    private val id by lazy {
        intent.getStringExtra(AppConstants.WEBSERIES_ID)!!
    }
    private val name by lazy {
        intent.getStringExtra(AppConstants.WEBSERIES_NAME)!!
    }
    private val thumbnail by lazy {
        intent.getStringExtra(AppConstants.WEBSERIES_THUMBNAIL)!!
    }

    companion object {
        @JvmStatic
        fun createIntent(ctx: Context, id: String, name: String, thumbnail: String): Intent =
            Intent(ctx, WebSeriesDetailActivity::class.java)
                .putExtra(AppConstants.WEBSERIES_ID, id)
                .putExtra(AppConstants.WEBSERIES_NAME, name)
                .putExtra(AppConstants.WEBSERIES_THUMBNAIL, thumbnail)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_series_detail)
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        binding.toolbarLayout.apply {
            setCollapsedTitleTypeface(
                ResourcesCompat.getFont(this@WebSeriesDetailActivity, R.font.inter_regular)
            )
            setExpandedTitleTypeface(
                ResourcesCompat.getFont(this@WebSeriesDetailActivity, R.font.inter_bold)
            )
        }
        binding.vm = vm
        vm.setArgs(id, name, thumbnail)
        trackWebseriesPageOpened()
        init()
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Error -> {
                    errorHandler(it.throwable)
                }
            }
        })
    }

    fun init() {
        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this)
        val viewAdapter = RecyclerViewAdapter<BaseModel>(
            vm.items, videoGroupViewProvider, videoGroupVmBinder
        )
        recyclerView.adapter = viewAdapter
        recyclerView.layoutManager = layoutManager
        vm.fetchWebSeriesDetail(id)
    }

    private val videoGroupViewProvider = ViewProviders.wrapSequentially(
        ViewProviders.progressViewProvider(),
        ViewProviders.dummyViewProvider(),
        ViewProviders.videoGroupItemViewProvider(),
        viewProvider { vm ->
            when (vm) {
                is WebSeriesDetailItemVm -> R.layout.item_web_series_detail
                is SeasonEpisodeItemVm -> R.layout.item_season_episode
                is SeasonHeaderVm -> R.layout.item_season_header
                else -> argumentError()
            }
        })

    private val callbacks: WebSeriesDetailCallbacks = object : WebSeriesDetailCallbacks {
        override fun onVideoClick(vm: VideoVm?) {
            vm?.let {
                onVideoClick.invoke(it)
                trackWebseriesPlayCTAClicked()
            }
        }

        override fun onShareClick(vm: WebSeriesDetailItemVm) {
            val intent = shareTextIntent(
                getString(R.string.share_sheet_title, vm.name),
                getString(
                    R.string.web_series_share_text, vm.name, playStoreUrl()
                )
            )
            startActivity(intent)
            trackWebseriesShareClicked()
        }
    }

    private val onVideoClick = { vm: VideoVm ->
        openVideoPlayerPage(
            this, vm.id, vm.name, vm.thumbnail, vm.url!!, vm.duration, vm.lastPlayedTime,
            vm.adTimes, vm.showAds!!, vm.shouldPlay!!, vm.planUid
        )
    }

    private val onEpisodeVideoClick = { vm: SeasonEpisodeItemVm ->
        openVideoPlayerPage(
            this, vm.id, vm.title, vm.imageUrl, vm.url!!, vm.durationInMs, vm.lastPlayedTime,
            vm.adTimes, vm.showAds!!, vm.shouldPlay!!, vm.planUid
        )
        trackWebseriesEpisodeClicked(vm)
    }

    private val onSeasonSelected = { seasonPair: Pair<String, String> ->
        vm.onSeasonSelected(seasonPair)
        trackWebseriesSeasonsClicked(seasonPair)
    }

    private val videoGroupVmBinder = viewModelBinder { itemBinding, vm1 ->
        when (vm1) {
            is WebSeriesDetailItemVm -> {
                (itemBinding as ItemWebSeriesDetailBinding).vm = vm1
                itemBinding.callback = callbacks
            }
            is SeasonEpisodeItemVm -> {
                (itemBinding as ItemSeasonEpisodeBinding).transformations =
                    getDefaultTransformations()
                itemBinding.onVideoClick = onEpisodeVideoClick
            }
            is SeasonHeaderVm -> {
                (itemBinding as ItemSeasonHeaderBinding).onItemClick = onSeasonSelected
            }
        }
    }

    private fun trackWebseriesPageOpened() {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.WEBSERIES_ID, id)
            putString(AnalyticsConstants.Property.WEBSERIES_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.WEBSERIES_PAGE_OPENED, bundle)
    }

    private fun trackWebseriesPlayCTAClicked() {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.WEBSERIES_ID, id)
            putString(AnalyticsConstants.Property.WEBSERIES_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.WEBSERIES_PLAY_CTA_CLICKED, bundle)
    }

    private fun trackWebseriesShareClicked() {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.WEBSERIES_ID, id)
            putString(AnalyticsConstants.Property.WEBSERIES_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.WEBSERIES_SHARE_CLICKED, bundle)
    }

    private fun trackWebseriesEpisodeClicked(vm: SeasonEpisodeItemVm) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.WEBSERIES_ID, id)
            putString(AnalyticsConstants.Property.WEBSERIES_NAME, name)
            putString(AnalyticsConstants.Property.SEASON_ID, vm.seasonId)
            putString(AnalyticsConstants.Property.SEASON_NAME, vm.seasonName)
            putString(AnalyticsConstants.Property.VIDEO_ID, vm.id)
            putString(AnalyticsConstants.Property.VIDEO_NAME, vm.title)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.WEBSERIES_EPISODE_CLICKED, bundle)
    }

    private fun trackWebseriesSeasonsClicked(seasonPair: Pair<String, String>) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.WEBSERIES_ID, id)
            putString(AnalyticsConstants.Property.WEBSERIES_NAME, name)
            putString(AnalyticsConstants.Property.SEASON_ID, seasonPair.first)
            putString(AnalyticsConstants.Property.SEASON_NAME, seasonPair.second)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.WEBSERIES_SEASONS_CLICKED, bundle)
    }
}
