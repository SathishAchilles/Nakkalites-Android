package `in`.nakkalites.mediaclient.view.home

import `in`.nakkalites.mediaclient.BR
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.databinding.*
import `in`.nakkalites.mediaclient.domain.models.BannerType
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.*
import `in`.nakkalites.mediaclient.view.binding.BindingPagerAdapter.PageTitles
import `in`.nakkalites.mediaclient.view.binding.ViewProviders.dummyViewProvider
import `in`.nakkalites.mediaclient.view.binding.ViewProviders.progressViewProvider
import `in`.nakkalites.mediaclient.view.binding.ViewProviders.videoGroupItemViewProvider
import `in`.nakkalites.mediaclient.view.utils.*
import `in`.nakkalites.mediaclient.view.videogroup.VideoGroupListActivity
import `in`.nakkalites.mediaclient.view.webseries.WebSeriesDetailActivity
import `in`.nakkalites.mediaclient.view.webview.WebViewActivity
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.home.*
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesListVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesVm
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    val vm: HomeVm by viewModel()
    val analyticsManager by inject<AnalyticsManager>()

    companion object {
        @JvmStatic
        fun createIntent(ctx: Context) =
            Intent(ctx, HomeActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        setupToolbar(binding.toolbar, showHomeAsUp = false, upIsBack = false)
        init()
        trackHomePageOpened()
        vm.allVideosGroupsStates().observe(this, EventObserver {
            when (it) {
                is Result.Error -> {
                    errorHandler(it.throwable)
                }
            }
        })
        vm.webSeriesStates().observe(this, EventObserver {
            when (it) {
                is Result.Error -> {
                    errorHandler(it.throwable)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.home, menu)
        return true
    }

    private fun init() {
        val pagerAdapter = BindingPagerAdapter(vm.pages, pageViewProvider, pageViewBinder)
            .also { it.setPageTitles(pageTitles) }
        binding.let {
            it.vm = vm
            it.onTabSelect =
                object : TabLayout.ViewPagerOnTabSelectedListener(it.viewPager) {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        super.onTabSelected(tab)
                        vm.setSelectedTab(tab.position)
                        val homeTab = HomeTab.fromPosition(tab.position)
                        if (homeTab == HomeTab.ALL) {
                            trackHomePageOpened()
                        } else {
                            trackWebSeriesTabOpened()
                        }
                    }
                }
            it.viewPager.adapter = pagerAdapter
            it.viewPager.currentItem = vm.selectedTab.position
            it.toolbar.setOnMenuItemClickListener(toolbarMenuListener)
        }
    }

    private val pageTitles = object : PageTitles<BaseViewModel> {
        override fun getPageTitle(position: Int, item: BaseViewModel) =
            getString(vm.pageTitleRes[position])
    }

    private val pageViewProvider = viewProvider { R.layout.page_home }

    private val pageViewBinder = viewModelBinder { binding1: ViewDataBinding, vm1: BaseModel ->
        when (vm1) {
            is AllVideoGroupsVm -> {
                val recyclerView = (binding1 as PageHomeBinding).recyclerView
                val scrollPager = RecyclerViewScrollPager(this,
                    { recyclerView }, Runnable { vm1.fetchVideoGroups() },
                    { vm1.loading() }, false
                )
                val linearLayoutManager = LinearLayoutManager(this)
                val viewAdapter =
                    RecyclerViewAdapter(vm1.items, videoGroupViewProvider, videoGroupVmBinder)
                with(recyclerView) {
                    layoutManager = linearLayoutManager
                    adapter = viewAdapter
                }
                scrollPager.attachScrollEvent()
                vm1.initPagingBody(scrollPager.pagingCallback)
                vm1.fetchVideoGroups()
                binding1.swipeRefresh.setDefaultColors()
                binding1.swipeRefresh.setOnRefreshListener {
                    vm1.refreshList()
                }
                binding1.isRefreshing = vm1.isRefreshing
            }
            is WebSeriesListVm -> {
                val recyclerView = (binding1 as PageHomeBinding).recyclerView
                val scrollPager = RecyclerViewScrollPager(this,
                    { recyclerView }, Runnable { vm1.fetchWebSeriesList() },
                    { vm1.loading() }, false
                )
                val linearLayoutManager = LinearLayoutManager(this)
                val viewAdapter =
                    RecyclerViewAdapter(vm1.items, webSeriesViewProvider, webSeriesVmBinder)
                with(recyclerView) {
                    layoutManager = linearLayoutManager
                    adapter = viewAdapter
                }
                scrollPager.attachScrollEvent()
                vm1.initPagingBody(scrollPager.pagingCallback)
                vm1.fetchWebSeriesList()
                binding1.swipeRefresh.setDefaultColors()
                binding1.swipeRefresh.setOnRefreshListener {
                    vm1.refreshList()
                }
                binding1.isRefreshing = vm1.isRefreshing
            }
            else -> argumentError()
        }
    }

    private val videoGroupViewProvider = ViewProviders.wrapSequentially(
        progressViewProvider(), dummyViewProvider(), videoGroupItemViewProvider(),
        viewProvider { vm1: BaseModel ->
            when (vm1) {
                is BannersVm -> R.layout.item_banners
                else -> argumentError()
            }
        })

    private val videoGroupVmBinder = viewModelBinder { itemBinding, vm1 ->
        when (vm1) {
            is BannersVm -> {
                val viewPager: ViewPager = (itemBinding as ItemBannersBinding).viewPager
                if (viewPager.adapter == null) {
                    viewPager.adapter = BindingPagerAdapter<BaseModel>(
                        vm1.items, bannerProvider, bannerBinder
                    )
                }
                viewPager.clipToPadding = false
                viewPager.pageMargin = dpToPx(30)
                viewPager.offscreenPageLimit = 2
                val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
                val currentItemHorizontalMarginPx =
                    resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
                val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
                val pageTransformer = ViewPager.PageTransformer { page: View, position: Float ->
                    page.translationX = -pageTranslationX * position
//                    page.scaleY = 1 - (0.25f * abs(position))
                    // If you want a fading effect uncomment the next line:
//                     page.alpha = 0.25f + (1 - abs(position))
                }
                viewPager.setPageTransformer(false, pageTransformer)

            }
            is VideoGroupVm -> {
                ViewModelBinders.mapViewGroupVmBinding(
                    this, onVideoClick, itemBinding, vm1, dpToPx(150), dpToPx(250), true,
                    onVideoGroupClick
                )
            }
        }
    }

    private val onVideoGroupClick = { vm: VideoGroupVm ->
        startActivity(VideoGroupListActivity.createIntent(this, vm.id, vm.name, vm.category))
        trackVideoGroupClicked(vm.id, vm.name)
    }

    private val onVideoClick = { vm: VideoVm ->
        openVideoDetailPage(this, vm.id, vm.name, vm.thumbnail, vm.url)
        trackVideoClicked(vm.id, vm.name)
    }

    private val bannerProvider = viewProvider { R.layout.item_banner }

    private val bannerBinder = viewModelBinder { viewDataBinding, vm ->
        viewDataBinding.setVariable(BR.vm, vm)
        when (vm) {
            is BannerVm -> {
                (viewDataBinding as ItemBannerBinding).onBannerClick = onBannerClick
                viewDataBinding.transformations = getDefaultTransformations()
            }
        }
    }

    private val onBannerClick = { vm: BannerVm ->
        vm.type?.let { type ->
            when (type) {
                BannerType.WEB_SERIES -> onWebSeriesClick(vm.webSeriesVm!!)
                BannerType.VIDEO -> {
                    vm.videoVm?.let { videoVm ->
                        openVideoDetailPage(
                            this, videoVm.id, videoVm.name, videoVm.thumbnail, videoVm.url
                        )
                    }
                    Unit
                }
            }
            trackBannerClicked(
                vm.id, vm.name, vm.type, vm.videoVm?.id, vm.videoVm?.name, vm.webSeriesVm?.id,
                vm.webSeriesVm?.name
            )
        }
    }

    private val webSeriesViewProvider = ViewProviders.wrapSequentially(
        progressViewProvider(), dummyViewProvider(), viewProvider { vm1: BaseModel ->
            when (vm1) {
                is WebSeriesVm -> R.layout.item_web_series
                else -> argumentError()
            }
        })

    private val webSeriesVmBinder = viewModelBinder { viewDataBinding, vm1 ->
        when (vm1) {
            is WebSeriesVm -> {
                (viewDataBinding as ItemWebSeriesBinding).onWebSeriesClick = onWebSeriesClick
                viewDataBinding.transformations = getDefaultTransformations()
            }
        }
    }

    private val onWebSeriesClick = { vm1: WebSeriesVm ->
        trackWebseriesClicked(vm1.id, vm1.name)
        startActivity(WebSeriesDetailActivity.createIntent(this, vm1.id, vm1.name, vm1.thumbnail))
    }

    private val toolbarMenuListener = { menuItem: MenuItem ->
        fun createWebViewIntent(url: String, @StringRes toolbarTitleRes: Int) =
            WebViewActivity.createIntent(this, name = getString(toolbarTitleRes), url = url)

        val intent = when (menuItem.itemId) {
            R.id.open_source_licenses ->
                createWebViewIntent(
                    getString(R.string.licenses_html_path), R.string.open_source_licenses
                )
            R.id.terms_conditions ->
                Intent(Intent.ACTION_VIEW, Uri.parse(HttpConstants.TERMS_CONDITIONS))
            R.id.privacy_policy ->
                Intent(Intent.ACTION_VIEW, Uri.parse(HttpConstants.PRIVACY_POLICY))
            R.id.contact_us_mail -> {
                trackEmailClicked()
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.type = "text/plain"
                intent.data = Uri.parse("mailto:${AppConstants.CONTACT_EMAIL}");
                Intent.createChooser(intent, "Send Email to Nakkalites via")
            }
            else -> null
        }
        try {
            intent?.let(::startActivity)?.let { true } ?: false
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    private fun trackHomePageOpened() {
        analyticsManager.logEvent(AnalyticsConstants.Event.HOME_TAB_OPENED)
    }

    private fun trackWebSeriesTabOpened() {
        analyticsManager.logEvent(AnalyticsConstants.Event.WEBSERIES_TAB_OPENED)
    }

    private fun trackVideoGroupClicked(id: String, name: String) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.VIDEO_GROUP_ID, id)
            putString(AnalyticsConstants.Property.VIDEO_GROUP_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.HOME_VIDEO_GROUP_CLICKED, bundle)
    }

    private fun trackVideoClicked(id: String, name: String) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.VIDEO_ID, id)
            putString(AnalyticsConstants.Property.VIDEO_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.HOME_VIDEO_CLICKED, bundle)
    }

    private fun trackBannerClicked(
        id: String, name: String, type: BannerType, videoId: String?, videoName: String?,
        webSeriesId: String?, webSeriesName: String?
    ) {
        val bundle = Bundle().apply {
            if (type == BannerType.VIDEO) {
                putString(AnalyticsConstants.Property.VIDEO_ID, videoId)
                putString(AnalyticsConstants.Property.VIDEO_NAME, videoName)
            } else if (type == BannerType.WEB_SERIES) {
                putString(AnalyticsConstants.Property.WEBSERIES_ID, webSeriesId)
                putString(AnalyticsConstants.Property.WEBSERIES_NAME, webSeriesName)
            }
            putString(AnalyticsConstants.Property.BANNER_ID, id)
            putString(AnalyticsConstants.Property.BANNER_NAME, name)
            putString(AnalyticsConstants.Property.BANNER_TYPE, type.name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.BANNER_CLICKED, bundle)
    }

    private fun trackEmailClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.EMAIL_CLICKED)
    }

    private fun trackWebseriesClicked(id: String, name: String) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.WEBSERIES_ID, id)
            putString(AnalyticsConstants.Property.WEBSERIES_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.WEBSERIES_CLICKED, bundle)
    }
}
