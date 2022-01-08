package `in`.nakkalites.mediaclient.view.home

import `in`.nakkalites.mediaclient.BR
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.FragmentAllVideosBinding
import `in`.nakkalites.mediaclient.databinding.ItemBannerBinding
import `in`.nakkalites.mediaclient.databinding.ItemBannersBinding
import `in`.nakkalites.mediaclient.domain.models.BannerType
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseFragment
import `in`.nakkalites.mediaclient.view.binding.*
import `in`.nakkalites.mediaclient.view.utils.*
import `in`.nakkalites.mediaclient.view.videogroup.VideoGroupListActivity
import `in`.nakkalites.mediaclient.view.webseries.WebSeriesDetailActivity
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.home.AllVideoGroupsVm
import `in`.nakkalites.mediaclient.viewmodel.home.BannerVm
import `in`.nakkalites.mediaclient.viewmodel.home.BannersVm
import `in`.nakkalites.mediaclient.viewmodel.home.HomeTab
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesVm
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.rd.utils.DensityUtils.dpToPx
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AllVideosFragment : BaseFragment() {
    private lateinit var binding: FragmentAllVideosBinding
    val vm: AllVideoGroupsVm by viewModel()

    val analyticsManager by inject<AnalyticsManager>()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentAllVideosBinding.inflate(inflater)
        val recyclerView = binding.recyclerView
        val scrollPager = RecyclerViewScrollPager(
                this, { recyclerView }, Runnable { vm.fetchVideoGroups() }, { vm.loading() }, false
        )
        val linearLayoutManager = LinearLayoutManager(requireContext())
        val viewAdapter = RecyclerViewAdapter(vm.items, videoGroupViewProvider, videoGroupVmBinder)
        with(recyclerView) {
            layoutManager = linearLayoutManager
            adapter = viewAdapter
        }
        scrollPager.attachScrollEvent()
        vm.initPagingBody(scrollPager.pagingCallback)
        vm.fetchVideoGroups()
        binding.swipeRefresh.setDefaultColors()
        binding.swipeRefresh.setOnRefreshListener {
            vm.refreshList()
        }
        binding.isRefreshing = vm.isRefreshing
        binding.vm = vm
        binding.onAllVideosPageRetry = refreshPage
        vm.viewStates().observe(requireActivity(), EventObserver {
            if (it is Result.Error) {
                activity?.errorHandler(it.throwable)
            }
        })
        (activity as? HomeActivity)?.addTabReselectedListener(HomeTab.ALL) { vm.refreshList() }
        return binding.root
    }

    private val videoGroupViewProvider = ViewProviders.wrapSequentially(
            ViewProviders.progressViewProvider(),
            ViewProviders.dummyViewProvider(),
            ViewProviders.videoGroupItemViewProvider(),
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
                        requireContext(), onVideoClick, itemBinding, vm1, dpToPx(150), dpToPx(250),
                        true, onVideoGroupClick
                )
            }
        }
    }

    private val refreshPage = {
        vm.refreshList()
    }

    private val onVideoGroupClick = { vm: VideoGroupVm ->
        startActivity(
                VideoGroupListActivity.createIntent(requireContext(), vm.id, vm.name, vm.category)
        )
        trackVideoGroupClicked(vm.id, vm.name)
    }

    private val onVideoClick = { vm: VideoVm ->
        openVideoDetailPage(requireContext(), vm.id, vm.name, vm.thumbnail, vm.url)
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
                                requireContext(), videoVm.id, videoVm.name, videoVm.thumbnail,
                                videoVm.url
                        )
                    }
                }
            }
            trackBannerClicked(
                    vm.id, vm.name, type, vm.videoVm?.id, vm.videoVm?.name, vm.webSeriesVm?.id,
                    vm.webSeriesVm?.name
            )
        }
    }

    private val onWebSeriesClick = { vm1: WebSeriesVm ->
        trackWebseriesClicked(vm1.id, vm1.name)
        startActivity(
                WebSeriesDetailActivity.createIntent(requireContext(), vm1.id, vm1.name, vm1.thumbnail)
        )
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

    private fun trackWebseriesClicked(id: String, name: String) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.WEBSERIES_ID, id)
            putString(AnalyticsConstants.Property.WEBSERIES_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.WEBSERIES_CLICKED, bundle)
    }

    companion object {
        @JvmStatic
        fun newInstance() = AllVideosFragment()
    }
}
