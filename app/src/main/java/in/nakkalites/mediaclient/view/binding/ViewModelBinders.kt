package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.databinding.ItemVideoBinding
import `in`.nakkalites.mediaclient.view.utils.dpToPx
import `in`.nakkalites.mediaclient.view.utils.getDefaultTransformations
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.video.VideoVm
import android.content.Context
import androidx.databinding.ViewDataBinding

object ViewModelBinders {
    @JvmStatic
    fun videoViewModelProvider(
        context: Context, height: Int, width: Int, onVideoClick: (VideoVm) -> Unit
    ): ViewModelBinder = object : ViewModelBinder {
        override fun bind(viewDataBinding: ViewDataBinding, viewModel: BaseModel) {
            when (viewModel) {
                is VideoVm -> {
                    (viewDataBinding as ItemVideoBinding).onVideoClick =
                        { onVideoClick.invoke(viewModel) }
                    viewDataBinding.vm = viewModel
                    viewDataBinding.transformations = getDefaultTransformations()
                    viewDataBinding.height = context.dpToPx(height)
                    viewDataBinding.width = context.dpToPx(width)
                }
            }
        }
    }
}
