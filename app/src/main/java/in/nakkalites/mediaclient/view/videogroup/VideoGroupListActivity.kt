package `in`.nakkalites.mediaclient.view.videogroup

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
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
import org.koin.androidx.viewmodel.ext.android.viewModel

class VideoGroupListActivity : BaseActivity() {

    private lateinit var binding: ActivityVideoGroupListBinding
    val vm by viewModel<VideoGroupListVm>()
    private val videoGroupId by lazy {
        intent.getStringExtra(AppConstants.VIDEO_GROUP_ID)
    }
    private val videoGroupName by lazy {
        intent.getStringExtra(AppConstants.VIDEO_GROUP_NAME)
    }

    companion object {
        @JvmStatic
        fun createIntent(ctx: Context, videoGroupId: String, videoGroupName: String): Intent =
            Intent(ctx, VideoGroupListActivity::class.java)
                .putExtra(AppConstants.VIDEO_GROUP_ID, videoGroupId)
                .putExtra(AppConstants.VIDEO_GROUP_NAME, videoGroupName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_group_list)
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        binding.vm = vm
        vm.setArgs(videoGroupId, videoGroupName)
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
            { recyclerView }, Runnable { vm.fetchVideoGroups(videoGroupId, videoGroupName) },
            { vm.loading() }, false
        )
        val gridLayoutManager = LinearLayoutManager(this)
        val viewAdapter = RecyclerViewAdapter<BaseModel>(
            vm.items, videoViewProvider,
            ViewModelBinders.videoViewModelProvider(
                this, dpToPx(250), displayWidth() - dpToPx(40), onVideoClick
            )
        )
        recyclerView.adapter = viewAdapter
        recyclerView.layoutManager = gridLayoutManager
        scrollPager.attachScrollEvent()
        vm.initPagingBody(scrollPager.pagingCallback)
        vm.fetchVideoGroups(videoGroupId, videoGroupName)
    }

    private val onVideoClick = { vm: VideoVm ->
        loge("Video clicked ${vm.name}")
        openVideoDetailPage(this, vm.id, vm.name, vm.thumbnail, vm.url)
    }

    private val videoViewProvider = ViewProviders.wrapSequentially(
        ViewProviders.progressViewProvider(), ViewProviders.dummyViewProvider(),
        videoItemViewProvider(), viewProvider { argumentError() })
}
