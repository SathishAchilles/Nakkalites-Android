package `in`.nakkalites.mediaclient.view.webview

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.databinding.ActivityWebViewBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.viewmodel.webview.WebViewVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class WebViewActivity : BaseActivity() {
    private lateinit var binding: ActivityWebViewBinding
    val vm by viewModel<WebViewVm>()
    private val title by lazy {
        intent.getStringExtra(AppConstants.WEB_VIEW_TITLE)
    }
    private val url by lazy {
        intent.getStringExtra(AppConstants.WEB_VIEW_URL)
    }

    companion object {
        @JvmStatic
        fun createIntent(
            ctx: Context, name: String, url: String
        ): Intent =
            Intent(ctx, WebViewActivity::class.java)
                .putExtra(AppConstants.WEB_VIEW_TITLE, name)
                .putExtra(AppConstants.WEB_VIEW_URL, url)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)
        binding.vm = vm
        setupToolbar(binding.toolbar, showHomeAsUp = true, upIsBack = true)
        vm.setArgs(title, url)
    }
}
