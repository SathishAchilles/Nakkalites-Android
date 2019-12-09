package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.databinding.ItemVideoBinding
import `in`.nakkalites.mediaclient.databinding.ItemVideoGroupBinding
import `in`.nakkalites.mediaclient.view.utils.dpToPx
import `in`.nakkalites.mediaclient.view.utils.getDefaultTransformations
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import `in`.nakkalites.mediaclient.viewmodel.videogroup.VideoGroupVm
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object ViewModelBinders {
    @JvmStatic
    fun videoViewModelProvider(
        context: Context, height: Int, width: Int, onVideoClick: (VideoVm) -> Unit
    ): ViewModelBinder = object : ViewModelBinder {
        override fun bind(viewDataBinding: ViewDataBinding, viewModel: BaseModel) {
            when (viewModel) {
                is VideoVm -> {
                    (viewDataBinding as ItemVideoBinding).onVideoClick = onVideoClick
                    viewDataBinding.vm = viewModel
                    loge(viewModel.name)
                    viewDataBinding.transformations = getDefaultTransformations()
                    viewDataBinding.height = height
                    viewDataBinding.width = width
                }
            }
        }
    }

    @JvmStatic
    fun videoGroupViewModelProvider(
        context: Context, onVideoClick: (VideoVm) -> Unit,
        onVideoGroupClick: (VideoGroupVm) -> Unit = {}, showDeeplinkArrow: Boolean
    ): ViewModelBinder = object : ViewModelBinder {
        override fun bind(viewDataBinding: ViewDataBinding, viewModel: BaseModel) {
            when (viewModel) {
                is VideoGroupVm -> mapViewGroupVmBinding(
                    context, onVideoClick, viewDataBinding, viewModel, showDeeplinkArrow,
                    onVideoGroupClick
                )
            }
        }
    }

    @JvmStatic
    fun mapViewGroupVmBinding(
        context: Context, onVideoClick: (VideoVm) -> Unit, viewDataBinding: ViewDataBinding,
        viewModel: VideoGroupVm, showDeeplinkArrow: Boolean = true,
        onVideoGroupClick: (VideoGroupVm) -> Unit = {}
    ) {
        val binding = (viewDataBinding as ItemVideoGroupBinding)
        binding.showDeeplinkArrow = showDeeplinkArrow
        val linearLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        val viewAdapter = RecyclerViewAdapter(
            viewModel.items, ViewProviders.videoItemViewProvider(),
            videoViewModelProvider(context, context.dpToPx(150), context.dpToPx(250), onVideoClick)
        )
        with(binding.recyclerView) {
            layoutManager = linearLayoutManager
            adapter = viewAdapter
        }
        binding.onVideoGroupClick = onVideoGroupClick
    }
}
