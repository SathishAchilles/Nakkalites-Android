package `in`.nakkalites.mediaclient.view.video

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.ActivityVideoDetailBinding
import `in`.nakkalites.mediaclient.databinding.ItemVideoDetailBinding
import `in`.nakkalites.mediaclient.databinding.ItemVideoGridBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.*
import `in`.nakkalites.mediaclient.view.utils.*
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.video.VideoDetailItemVm
import `in`.nakkalites.mediaclient.viewmodel.video.VideoDetailVm
import `in`.nakkalites.mediaclient.viewmodel.video.VideoListHeader
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.recyclerview.widget.GridLayoutManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class VideoDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityVideoDetailBinding
    val vm by viewModel<VideoDetailVm>()
    val analyticsManager by inject<AnalyticsManager>()
    private val id : String by lazy {
        intent.getStringExtra(AppConstants.VIDEO_ID)!!
    }
    private val name by lazy {
        intent.getStringExtra(AppConstants.VIDEO_NAME)!!
    }
    private val thumbnail by lazy {
        intent.getStringExtra(AppConstants.VIDEO_THUMBNAIL)!!
    }
    private val url by lazy {
        intent.getStringExtra(AppConstants.VIDEO_URL)
    }
    private var menu: Menu? = null
    private val spanCount = 2

    companion object {
        @JvmStatic
        fun createIntent(
            ctx: Context, id: String, name: String, thumbnail: String, url: String?
        ): Intent = Intent(ctx, VideoDetailActivity::class.java)
            .putExtra(AppConstants.VIDEO_ID, id)
            .putExtra(AppConstants.VIDEO_NAME, name)
            .putExtra(AppConstants.VIDEO_THUMBNAIL, thumbnail)
            .putExtra(AppConstants.VIDEO_URL, url)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_detail)
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        binding.vm = vm
        binding.onPlayClick = onPlayClick
        vm.setArgs(id, name, thumbnail, url)
        trackVideoDetailPageOpened()
        init()
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Error -> {
                    errorHandler(it.throwable)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_share, menu)
        hideOption(R.id.action_share)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.action_share) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hideOption(id: Int) {
        val item = menu?.findItem(id)
        item?.isVisible = false
    }

    private fun showOption(id: Int) {
        val item = menu?.findItem(id)
        item?.isVisible = true
    }

    fun init() {
//        binding.appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
//            var isShow = false
//            var scrollRange = -1
//            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
//                if (scrollRange == -1) {
//                    scrollRange = appBarLayout.totalScrollRange
//                }
//                if (scrollRange + verticalOffset == 0) {
//                    isShow = true
//                    showOption(R.id.action_share)
//                } else if (isShow) {
//                    isShow = false
//                    hideOption(R.id.action_share)
//                }
//            }
//        })
        val recyclerView = binding.recyclerView
        val scrollPager = RecyclerViewScrollPager(this,
            { recyclerView }, Runnable { vm.fetchVideoDetail(id) },
            { vm.loading() }, false
        )
        val gridLayoutManager = GridLayoutManager(this, spanCount)
        val viewAdapter = RecyclerViewAdapter<BaseModel>(vm.items, viewProvider, vmBinder)
        binding.spanCount = spanCount
        binding.spanSizeLookup = spanSizeLookup(vm.items)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = viewAdapter
        scrollPager.attachScrollEvent()
        vm.initPagingBody(scrollPager.pagingCallback)
        vm.fetchVideoDetail(id)
    }

    private val viewProvider = ViewProviders.wrapSequentially(
        ViewProviders.progressViewProvider(),
        ViewProviders.dummyViewProvider(),
        viewProvider { vm ->
            when (vm) {
                is VideoDetailItemVm -> R.layout.item_video_detail
                is VideoListHeader -> R.layout.item_video_list_header
                is VideoVm -> R.layout.item_video_grid
                else -> argumentError()
            }
        })

    private val onVideoClick = { vm: VideoVm ->
        openVideoPlayerPage(
            this,analyticsManager,vm.id, vm.name, vm.thumbnail, vm.url!!, vm.duration,
            vm.lastPlayedTime, vm.adTimes, vm.showAds!!, vm.shouldPlay!!, vm.planUid, vm.planName
        )
        trackVideoClicked(vm.id, vm.name)
    }

    private val onShareClick = { vm: VideoDetailItemVm ->
        val intent = shareTextIntent(
            getString(R.string.share_sheet_title, vm.name),
            getString(R.string.video_share_text, vm.name, playStoreUrl())
        )
        startActivity(intent)
        trackVideoShareClicked()
    }

    @SuppressLint("ClickableViewAccessibility")
    private val vmBinder = viewModelBinder { itemBinding, vm1 ->
        when (vm1) {
            is VideoDetailItemVm -> {
                (itemBinding as ItemVideoDetailBinding).vm = vm1
                itemBinding.onShareClick = onShareClick
                itemBinding.seekbar.setPadding(
                    itemBinding.seekbar.paddingStart, 0, itemBinding.seekbar.paddingEnd, 0
                )
                itemBinding.seekbar.setOnTouchListener { _, _ -> true }
                itemBinding.seekbar.thumb.mutate().alpha = 0
            }
            is VideoVm -> {
                (itemBinding as ItemVideoGridBinding).onVideoClick = onVideoClick
                itemBinding.vm = vm1
                itemBinding.transformations = getDefaultTransformations()
            }
        }
    }

    private val onPlayClick = { vm: VideoDetailVm ->
        openVideoPlayerPage(
            this,analyticsManager, vm.id!!, vm.name!!, vm.thumbnail!!, vm.url!!, vm.duration,
            vm.lastPlayedTime, vm.adTimes, vm.showAds!!, vm.shouldPlay!!, vm.planUid, vm.planName
        )
        trackVideoPlayCTAClicked(vm.id!!, vm.name!!)
    }

    private fun spanSizeLookup(items: ObservableArrayList<BaseModel>) =
        object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val vm1 = items[position]
                return if (vm1 is VideoVm) {
                    spanCount / 2
                } else {
                    spanCount
                }
            }
        }

    private fun trackVideoDetailPageOpened() {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.VIDEO_ID, id)
            putString(AnalyticsConstants.Property.VIDEO_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.VIDEO_DETAIL_PAGE_OPENED, bundle)
    }

    private fun trackVideoClicked(id: String, name: String) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.VIDEO_ID, id)
            putString(AnalyticsConstants.Property.VIDEO_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.VIDEO_DETAIL_VIDEO_CLICKED, bundle)
    }

    private fun trackVideoPlayCTAClicked(id: String, name: String) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.VIDEO_ID, id)
            putString(AnalyticsConstants.Property.VIDEO_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.VIDEO_DETAIL_PLAY_CTA_CLICKED, bundle)
    }

    private fun trackVideoShareClicked() {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.VIDEO_ID, id)
            putString(AnalyticsConstants.Property.VIDEO_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.VIDEO_DETAIL_SHARE_CTA_CLICKED, bundle)
    }
}
