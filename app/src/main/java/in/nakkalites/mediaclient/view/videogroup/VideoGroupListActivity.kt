package `in`.nakkalites.mediaclient.view.videogroup

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.ActivityVideoGroupListBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.*
import `in`.nakkalites.mediaclient.view.binding.ViewProviders.videoItemViewProvider
import `in`.nakkalites.mediaclient.view.utils.argumentError
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.utils.EmptyStateVm
import `in`.nakkalites.mediaclient.viewmodel.utils.ProgressBarVm
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupListVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.recyclerview.widget.GridLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class VideoGroupListActivity : BaseActivity() {

    private lateinit var binding: ActivityVideoGroupListBinding
    //    val vm: VideoGroupListVm by getViewModel<VideoGroupListVm>(parameters = mapOf(AppConstants.VIDEO_GROUP_ID to (intent.extras[AppConstants.VIDEO_GROUP_ID] as Int))
    val vm by viewModel<VideoGroupListVm>()
    private val videoGroupId by lazy {
        intent.getStringExtra(AppConstants.VIDEO_GROUP_ID)
    }
    private val videoGroupName by lazy {
        intent.getStringExtra(AppConstants.VIDEO_GROUP_NAME)
    }
    private val spanCount = 2

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
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = false)
        vm.setArgs(videoGroupId, videoGroupName)
        init()
    }

    private fun init() {
        val recyclerView = binding.recyclerView
        val scrollPager = RecyclerViewScrollPager(this,
            { recyclerView }, Runnable { vm.fetchVideoGroups(videoGroupId) },
            { vm.loading() }, false
        )
        val gridLayoutManager = GridLayoutManager(this, spanCount)
        val viewAdapter = RecyclerViewAdapter(
            vm.items, videoViewProvider,
            ViewModelBinders.videoViewModelProvider(this, 115, -1, onVideoClick)
        )
        recyclerView.adapter = viewAdapter
        binding.spanCount = spanCount
        binding.spanSizeLookup = spanSizeLookup(vm.items)
        recyclerView.layoutManager = gridLayoutManager
        scrollPager.attachScrollEvent()
        vm.initPagingBody(scrollPager.pagingCallback)
        vm.fetchVideoGroups(videoGroupId)
    }

    private val onVideoClick = { vm: VideoVm -> loge("Video clicked ${vm.name}") }

    private val videoViewProvider = ViewProviders.wrapSequentially(
        ViewProviders.progressViewProvider(), ViewProviders.dummyViewProvider(),
        videoItemViewProvider(), viewProvider { argumentError() })

    private fun spanSizeLookup(items: ObservableArrayList<BaseModel>) =
        object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val vm1 = items[position]
                return if (vm1 is ProgressBarVm || vm1 is EmptyStateVm) {
                    spanCount
                } else {
                    spanCount / 2
                }
            }
        }
}