package `in`.nakkalites.mediaclient.view.home

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.BR
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.*
import `in`.nakkalites.mediaclient.domain.models.BannerType
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.*
import `in`.nakkalites.mediaclient.view.binding.BindingPagerAdapter.PageTitles
import `in`.nakkalites.mediaclient.view.binding.ViewProviders.dummyViewProvider
import `in`.nakkalites.mediaclient.view.binding.ViewProviders.progressViewProvider
import `in`.nakkalites.mediaclient.view.binding.ViewProviders.videoGroupItemViewProvider
import `in`.nakkalites.mediaclient.view.utils.*
import `in`.nakkalites.mediaclient.view.videogroup.VideoGroupDetailActivity
import `in`.nakkalites.mediaclient.view.webseries.WebSeriesDetailActivity
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.home.*
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesListVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    val vm: HomeVm by viewModel()

    companion object {
        @JvmStatic
        fun createIntent(ctx: Context) =
            Intent(ctx, HomeActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        setupToolbar(binding.toolbar, showHomeAsUp = false, upIsBack = false)
        init()
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
                    }
                }
            it.viewPager.adapter = pagerAdapter
            it.viewPager.currentItem = vm.selectedTab.position
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
            }
            is VideoGroupVm -> {
                ViewModelBinders.mapViewGroupVmBinding(
                    this, onVideoClick, itemBinding, vm1, true, onVideoGroupClick
                )
            }
        }
    }

    private val onVideoGroupClick = { vm: VideoGroupVm ->
        loge("Video Group clicked ${vm.name}")
        startActivity(VideoGroupDetailActivity.createIntent(this, vm.id, vm.name))
    }

    private val onVideoClick = { vm: VideoVm ->
        loge("Video clicked ${vm.name}")
        NavigationUtil.openVideoDetailPage(this, vm.id, vm.name, vm.thumbnail)
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
        loge("Banner clicked ${vm.name}")
        when (vm.type) {
            BannerType.WEB_SERIES -> onWebSeriesClick(vm.webSeriesVm!!)
            BannerType.VIDEO -> onVideoClick(vm.videoVm!!)
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
        loge("Web Series clicked ${vm1.name}")
        startActivity(WebSeriesDetailActivity.createIntent(this, vm1.id, vm1.name, vm1.thumbnail))
    }
}
