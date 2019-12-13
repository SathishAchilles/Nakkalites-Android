package `in`.nakkalites.mediaclient.view.webseries

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.ActivityWebSeriesDetailBinding
import `in`.nakkalites.mediaclient.databinding.ItemWebSeriesDetailBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.*
import `in`.nakkalites.mediaclient.view.utils.argumentError
import `in`.nakkalites.mediaclient.view.utils.openVideoDetailPage
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesDetailItemVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesDetailVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class WebSeriesDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityWebSeriesDetailBinding
    val vm by viewModel<WebSeriesDetailVm>()
    private val id by lazy {
        intent.getStringExtra(AppConstants.WEBSERIES_ID)
    }
    private val name by lazy {
        intent.getStringExtra(AppConstants.WEBSERIES_NAME)
    }
    private val thumbnail by lazy {
        intent.getStringExtra(AppConstants.WEBSERIES_THUMBNAIL)
    }
    private var menu: Menu? = null


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
        binding.vm = vm
        vm.setArgs(id, name, thumbnail)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu;
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
//        binding.appBar.addOnOffsetChangedListener(object : OnOffsetChangedListener {
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
        vm.fetchWebSeriesDetail(id)
    }

    private val videoGroupViewProvider = ViewProviders.wrapSequentially(
        ViewProviders.progressViewProvider(),
        ViewProviders.dummyViewProvider(),
        ViewProviders.videoGroupItemViewProvider(),
        viewProvider { vm ->
            when (vm) {
                is WebSeriesDetailItemVm -> R.layout.item_web_series_detail
                else -> argumentError()
            }
        })

    private val onVideoClick = { vm: VideoVm ->
        loge("Video clicked ${vm.name}")
        openVideoDetailPage(this, vm.id, vm.name, vm.thumbnail, vm.url)
    }

    private val videoGroupVmBinder = viewModelBinder { itemBinding, vm1 ->
        when (vm1) {
            is WebSeriesDetailItemVm -> {
                (itemBinding as ItemWebSeriesDetailBinding).vm = vm1
                itemBinding.onVideoClick = onVideoClick
            }
            is VideoGroupVm -> {
                ViewModelBinders.mapViewGroupVmBinding(this, onVideoClick, itemBinding, vm1, false)
            }
        }
    }

}
