package `in`.nakkalites.mediaclient.view.binding

import androidx.lifecycle.ViewModel
import androidx.annotation.LayoutRes


interface ViewProvider {
    @LayoutRes
    fun getView(vm: ViewModel): Int
}
