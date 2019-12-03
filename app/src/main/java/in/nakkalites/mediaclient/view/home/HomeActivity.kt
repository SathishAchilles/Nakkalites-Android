package `in`.nakkalites.mediaclient.view.home

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.databinding.ActivityHomeBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.viewmodel.home.HomeVm
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    val vm: HomeVm by viewModel()

    companion object {
        @JvmStatic
        fun createIntent(ctx: Context) =
            Intent(ctx, HomeActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
    }
}
