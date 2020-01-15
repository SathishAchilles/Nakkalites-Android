package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.annotation.LayoutRes


interface ViewProvider {
    @LayoutRes
    fun getView(vm: BaseModel): Int
}

fun viewProvider(provider: (BaseModel) -> Int): ViewProvider =
    object : ViewProvider {
        override fun getView(vm: BaseModel): Int = provider(vm)
    }
