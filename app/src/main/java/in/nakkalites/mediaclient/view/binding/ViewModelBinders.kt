package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.R
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
                    viewDataBinding.transformations = getDefaultTransformations()
                    viewDataBinding.height = height
                    viewDataBinding.width = width
                    viewDataBinding.progressWidth = width - (context.dpToPx(
                        context.resources.getDimension(R.dimen.video_progress_padding).toInt()
                    ))
                }
            }
        }
    }

    @JvmStatic
    fun videoGroupViewModelProvider(
        context: Context, onVideoClick: (VideoVm) -> Unit,
        onVideoGroupClick: (VideoGroupVm) -> Unit = {}, height: Int, width: Int,
        showDeeplinkArrow: Boolean
    ): ViewModelBinder = object : ViewModelBinder {
        override fun bind(viewDataBinding: ViewDataBinding, viewModel: BaseModel) {
            when (viewModel) {
                is VideoGroupVm -> mapViewGroupVmBinding(
                    context, onVideoClick, viewDataBinding, viewModel, height, width,
                    showDeeplinkArrow, onVideoGroupClick
                )
            }
        }
    }

    @JvmStatic
    fun mapViewGroupVmBinding(
        context: Context, onVideoClick: (VideoVm) -> Unit, viewDataBinding: ViewDataBinding,
        viewModel: VideoGroupVm, height: Int, width: Int, showDeeplinkArrow: Boolean = true,
        onVideoGroupClick: (VideoGroupVm) -> Unit = {}
    ) {
        val binding = (viewDataBinding as ItemVideoGroupBinding)
        binding.showDeeplinkArrow = showDeeplinkArrow
        val linearLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        val viewAdapter = RecyclerViewAdapter(
            viewModel.items, ViewProviders.videoItemViewProvider(),
            videoViewModelProvider(context, height, width, onVideoClick)
        )
        with(binding.recyclerView) {
            layoutManager = linearLayoutManager
            adapter = viewAdapter
        }
        binding.onVideoGroupClick = onVideoGroupClick
    }
}
