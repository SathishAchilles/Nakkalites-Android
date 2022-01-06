package `in`.nakkalites.mediaclient.view.home

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.databinding.FragmentWebseriesBinding
import `in`.nakkalites.mediaclient.databinding.ItemWebSeriesBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseFragment
import `in`.nakkalites.mediaclient.view.binding.*
import `in`.nakkalites.mediaclient.view.binding.ViewProviders.dummyViewProvider
import `in`.nakkalites.mediaclient.view.binding.ViewProviders.progressViewProvider
import `in`.nakkalites.mediaclient.view.utils.*
import `in`.nakkalites.mediaclient.view.webseries.WebSeriesDetailActivity
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.home.HomeTab
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesListVm
import `in`.nakkalites.mediaclient.viewmodel.webseries.WebSeriesVm
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class WebseriesFragment : BaseFragment() {
    private lateinit var binding: FragmentWebseriesBinding
    val vm: WebSeriesListVm by viewModel()

    val analyticsManager by inject<AnalyticsManager>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentWebseriesBinding.inflate(inflater)
        val scrollPager = RecyclerViewScrollPager(
            this, { binding.recyclerView }, Runnable { vm.fetchWebSeriesList() },
            { vm.loading() }, false
        )
        val linearLayoutManager = LinearLayoutManager(requireContext())
        val viewAdapter =
            RecyclerViewAdapter(vm.items, webSeriesViewProvider, webSeriesVmBinder)
        with(binding.recyclerView) {
            layoutManager = linearLayoutManager
            adapter = viewAdapter
        }
        scrollPager.attachScrollEvent()
        vm.initPagingBody(scrollPager.pagingCallback)
        vm.fetchWebSeriesList()
        binding.swipeRefresh.setDefaultColors()
        binding.swipeRefresh.setOnRefreshListener {
            vm.refreshList()
        }
        binding.isRefreshing = vm.isRefreshing
        binding.vm = vm
        binding.onWebseriesAllVideosPageRetry = refreshPage
        vm.viewStates().observe(requireActivity(), EventObserver {
            if (it is Result.Error) {
                activity?.errorHandler(it.throwable)
            }
        })
        (activity as? HomeActivity)?.addTabReselectedListener(HomeTab.WEB_SERIES) {
            vm.refreshList()
        }
        return binding.root
    }

    private val refreshPage = {
        vm.refreshList()
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
        startActivity(
            WebSeriesDetailActivity.createIntent(requireContext(), vm1.id, vm1.name, vm1.thumbnail)
        )
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
        fun newInstance() = WebseriesFragment()
    }
}
