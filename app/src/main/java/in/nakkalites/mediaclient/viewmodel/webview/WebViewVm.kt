package `in`.nakkalites.mediaclient.viewmodel.webview

import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import androidx.databinding.ObservableField

class WebViewVm : BaseViewModel() {
    val toolbarTitle = ObservableField<String>()
    val url = ObservableField<String>()

    fun setArgs(title: String, url: String) {
        this.toolbarTitle.set(title)
        this.url.set(url)
    }
}
