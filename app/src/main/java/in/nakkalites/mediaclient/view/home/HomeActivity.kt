package `in`.nakkalites.mediaclient.view.home

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivityHomeBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.BindingPagerAdapter
import `in`.nakkalites.mediaclient.view.binding.BindingPagerAdapter.PageTitles
import `in`.nakkalites.mediaclient.view.binding.viewModelBinder
import `in`.nakkalites.mediaclient.view.binding.viewProvider
import `in`.nakkalites.mediaclient.view.utils.argumentError
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.home.AllVideoGroupsVm
import `in`.nakkalites.mediaclient.viewmodel.home.HomeVm
import `in`.nakkalites.mediaclient.viewmodel.home.WebSeriesVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
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

    private val pageViewBinder = viewModelBinder { binding1: ViewDataBinding, vm1: BaseViewModel ->
        when (vm1) {
            is AllVideoGroupsVm -> {

            }
            is WebSeriesVm -> {

            }
            else -> argumentError()
        }
    }

}
