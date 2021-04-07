package `in`.nakkalites.mediaclient.view.subscription

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivityFaqBinding
import `in`.nakkalites.mediaclient.databinding.ItemFaqBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.binding.RecyclerViewAdapter
import `in`.nakkalites.mediaclient.view.binding.ViewProviders
import `in`.nakkalites.mediaclient.view.binding.viewModelBinder
import `in`.nakkalites.mediaclient.view.binding.viewProvider
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.argumentError
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.subscription.FaqListVm
import `in`.nakkalites.mediaclient.viewmodel.subscription.FaqVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class FaqActivity : BaseActivity() {
    private lateinit var binding: ActivityFaqBinding
    private val vm: FaqListVm by viewModel()

    companion object {
        @JvmStatic
        fun createIntent(ctx: Context): Intent = Intent(ctx, FaqActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_faq)
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        binding.vm = vm
        vm.viewStates().observe(this, EventObserver {
            when (it) {
                is Result.Success -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
                is Result.Error -> {
                    errorHandler(it.throwable)
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
                else -> showLoading()
            }
        })
        init()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun init() {
        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this)
        val viewAdapter = RecyclerViewAdapter<BaseModel>(vm.items, viewProvider, vmBinder)
        recyclerView.adapter = viewAdapter
        recyclerView.layoutManager = layoutManager
        vm.fetchFaqs()
    }

    private val viewProvider = ViewProviders.wrapSequentially(
        viewProvider { vm1: BaseModel ->
            when (vm1) {
                is FaqVm -> R.layout.item_faq
                else -> argumentError()
            }
        })

    private val vmBinder = viewModelBinder { itemBinding, vm1 ->
        when (vm1) {
            is FaqVm -> {
                val binding = (itemBinding as ItemFaqBinding)
                binding.vm = vm1
                binding.onFaqExpanded = onFaqExpanded
            }
        }
    }

    private val onFaqExpanded = { imageView: View, _: View, vm: FaqVm ->
        imageView.animate()
            .rotation(if (vm.showAnswer.get()) 0F else 180F)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        vm.expand()
    }
}
