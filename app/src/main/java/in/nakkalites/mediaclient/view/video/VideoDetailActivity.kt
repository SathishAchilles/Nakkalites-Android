package `in`.nakkalites.mediaclient.view.video

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.ActivityVideoDetailBinding
import `in`.nakkalites.mediaclient.databinding.ItemVideoDetailBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.*
import `in`.nakkalites.mediaclient.view.utils.argumentError
import `in`.nakkalites.mediaclient.view.utils.openVideoDetailPage
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.video.VideoDetailItemVm
import `in`.nakkalites.mediaclient.viewmodel.video.VideoDetailVm
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class VideoDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityVideoDetailBinding
    val vm by viewModel<VideoDetailVm>()
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
    private var menu: Menu? = null


    companion object {
        @JvmStatic
        fun createIntent(
            ctx: Context, id: String, name: String, thumbnail: String, url: String
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
        init()
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
        val gridLayoutManager = LinearLayoutManager(this)
        val viewAdapter = RecyclerViewAdapter<BaseModel>(
            vm.items, videoGroupViewProvider, videoGroupVmBinder
        )
        recyclerView.adapter = viewAdapter
        recyclerView.layoutManager = gridLayoutManager
        vm.fetchVideoDetail(id)
    }

    private val videoGroupViewProvider = ViewProviders.wrapSequentially(
        ViewProviders.progressViewProvider(),
        ViewProviders.dummyViewProvider(),
        ViewProviders.videoGroupItemViewProvider(),
        viewProvider { vm ->
            when (vm) {
                is VideoDetailItemVm -> R.layout.item_video_detail
                else -> argumentError()
            }
        })

    private val onVideoClick = { vm: VideoVm ->
        loge("Video clicked ${vm.name}")
        openVideoDetailPage(this, vm.id, vm.name, vm.thumbnail, vm.url)
    }

    private val onShareClick = { vm: VideoDetailItemVm ->
        loge("Video share click ${vm.url}")
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/*")
            .putExtra(Intent.EXTRA_TEXT, vm.shareText)
            .let { Intent.createChooser(it, getString(R.string.share_sheet_title, vm.name)) }
        startActivity(intent)
    }

    private val videoGroupVmBinder = viewModelBinder { itemBinding, vm1 ->
        when (vm1) {
            is VideoDetailItemVm -> {
                (itemBinding as ItemVideoDetailBinding).vm = vm1
                itemBinding.onShareClick = onShareClick
            }
            is VideoGroupVm -> {
                ViewModelBinders.mapViewGroupVmBinding(this, onVideoClick, itemBinding, vm1, false)
            }
        }
    }

    private val onPlayClick = { vm: VideoDetailVm ->
        startActivity(
            VideoPlayerActivity.createIntent(this, vm.id!!, vm.name!!, vm.thumbnail!!, vm.url!!)
        )
    }
}
