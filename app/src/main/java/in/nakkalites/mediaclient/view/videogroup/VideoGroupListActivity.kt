package `in`.nakkalites.mediaclient.view.videogroup

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.ActivityVideoGroupListBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.*
import `in`.nakkalites.mediaclient.view.binding.ViewProviders.videoItemViewProvider
import `in`.nakkalites.mediaclient.view.utils.*
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupListVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class VideoGroupListActivity : BaseActivity() {

    private lateinit var binding: ActivityVideoGroupListBinding
    val vm by viewModel<VideoGroupListVm>()
    val analyticsManager by inject<AnalyticsManager>()
    private val videoGroupId by lazy {
        intent.getStringExtra(AppConstants.VIDEO_GROUP_ID)!!
    }
    private val videoGroupName by lazy {
        intent.getStringExtra(AppConstants.VIDEO_GROUP_NAME)!!
    }
    private val videoGroupCategory by lazy {
        intent.getStringExtra(AppConstants.VIDEO_GROUP_CATEGORY)!!
    }

    companion object {
        @JvmStatic
        fun createIntent(
            ctx: Context, videoGroupId: String, videoGroupName: String, category: String
        ): Intent = Intent(ctx, VideoGroupListActivity::class.java)
            .putExtra(AppConstants.VIDEO_GROUP_ID, videoGroupId)
            .putExtra(AppConstants.VIDEO_GROUP_NAME, videoGroupName)
            .putExtra(AppConstants.VIDEO_GROUP_CATEGORY, category)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_group_list)
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        binding.vm = vm
        vm.setArgs(videoGroupId, videoGroupName, videoGroupCategory)
        trackVideoGroupListPageOpened()
        init()
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Error -> {
                    errorHandler(it.throwable)
                }
            }
        })
    }

    private fun init() {
        val recyclerView = binding.recyclerView
        val scrollPager = RecyclerViewScrollPager(this,
            { recyclerView }, Runnable { vm.fetchVideoGroups(videoGroupId, videoGroupCategory) },
            { vm.loading() }, false
        )
        val gridLayoutManager = LinearLayoutManager(this)
        val viewAdapter = RecyclerViewAdapter<BaseModel>(
            vm.items, videoViewProvider,
            ViewModelBinders.videoViewModelProvider(
                this, dpToPx(250), displayWidth() - dpToPx(20), onVideoClick
            )
        )
        recyclerView.adapter = viewAdapter
        recyclerView.layoutManager = gridLayoutManager
        scrollPager.attachScrollEvent()
        vm.initPagingBody(scrollPager.pagingCallback)
        vm.fetchVideoGroups(videoGroupId, videoGroupCategory)
    }

    private val onVideoClick = { vm: VideoVm ->
        openVideoDetailPage(this, vm.id, vm.name, vm.thumbnail, vm.url)
        trackVideoGroupListVideoClicked(vm.id, vm.name)
    }

    private val videoViewProvider = ViewProviders.wrapSequentially(
        ViewProviders.progressViewProvider(), ViewProviders.dummyViewProvider(),
        videoItemViewProvider(), viewProvider { argumentError() })

    private fun trackVideoGroupListPageOpened() {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.VIDEO_GROUP_ID, videoGroupId)
            putString(AnalyticsConstants.Property.VIDEO_GROUP_NAME, videoGroupName)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.VIDEO_GROUP_LIST_PAGE_OPENED, bundle)
    }

    private fun trackVideoGroupListVideoClicked(id: String, name: String) {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.VIDEO_GROUP_ID, videoGroupId)
            putString(AnalyticsConstants.Property.VIDEO_GROUP_NAME, videoGroupName)
            putString(AnalyticsConstants.Property.VIDEO_ID, id)
            putString(AnalyticsConstants.Property.VIDEO_NAME, name)
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.VIDEO_GROUP_LIST_VIDEO_CLICKED, bundle)
    }
}
