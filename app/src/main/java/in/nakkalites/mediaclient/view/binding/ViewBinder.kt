package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.annotation.LayoutRes


interface ViewProvider {
    @LayoutRes
    fun getView(vm: BaseViewModel): Int
}

fun viewProvider(provider: (BaseViewModel) -> Int): ViewProvider =
    object : ViewProvider {
        override fun getView(vm: BaseViewModel): Int = provider(vm)
    }
