package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.BR
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.databinding.ViewDataBinding


interface ViewModelBinder {
    fun bind(viewDataBinding: ViewDataBinding, viewModel: BaseModel)

    companion object {
        val defaultBinder = viewModelBinder { binding, vm -> binding.setVariable(BR.vm, vm) }
    }
}


// Helper function to adapt kotlin SAMs as lambdas. See https://stackoverflow.com/a/33610615/1852422.
fun viewModelBinder(binder: (ViewDataBinding, BaseModel) -> Unit): ViewModelBinder =
    object : ViewModelBinder {
        override fun bind(viewDataBinding: ViewDataBinding, viewModel: BaseModel) =
            binder(viewDataBinding, viewModel)
    }

