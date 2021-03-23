package `in`.nakkalites.mediaclient.view.home

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.databinding.ActivityHomeBinding
import `in`.nakkalites.mediaclient.view.BaseActivity
import `in`.nakkalites.mediaclient.view.utils.formatEn
import `in`.nakkalites.mediaclient.view.webview.WebViewActivity
import `in`.nakkalites.mediaclient.viewmodel.home.HomeTab
import `in`.nakkalites.mediaclient.viewmodel.home.HomeVm
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*


class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    val vm: HomeVm by viewModel()
    val analyticsManager by inject<AnalyticsManager>()
    private var currentTab: HomeTab? = null
    private val tabReselectedListeners: MutableMap<HomeTab, Function0<Unit>?> = mutableMapOf()

    companion object {
        @JvmStatic
        fun createIntent(ctx: Context) = Intent(ctx, HomeActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        setExtrasToEmptyIfMalformed(intent)
        super.onCreate(savedInstanceState)
        if (!isTaskRoot && isFromLauncher(intent) && Intent.ACTION_MAIN == intent.action) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        handleOnCreate(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent) {
        setExtrasToEmptyIfMalformed(intent)
        super.onNewIntent(intent)
        setIntent(intent)
        handleNewIntent(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(AppConstants.BOTTOM_NAV_TAB, currentTab)
    }

    private fun handleOnCreate(savedInstanceState: Bundle?) {
        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem: MenuItem? ->
            onTabSelected(menuItem)
        }
        binding.bottomNavigation.setOnNavigationItemReselectedListener { menuItem: MenuItem? ->
            onTabReselected(menuItem)
        }
        handleNewIntent(savedInstanceState)
    }

    private fun handleNewIntent(savedInstanceState: Bundle?) {
        var tab: HomeTab? =
            intent.getSerializableExtra(AppConstants.BOTTOM_NAV_TAB) as HomeTab?
        if (savedInstanceState != null) {
            tab = savedInstanceState.getSerializable(AppConstants.BOTTOM_NAV_TAB) as HomeTab?
        }
        if (tab == null && currentTab == null) {
            tab = HomeTab.ALL
        }
        if (tab != null) {
            if (changeTab(tab)) {
                setTabSelected(tab)
            }
        }
    }

    private fun onTabReselected(menuItem: MenuItem?) {
        val newTab: HomeTab = HomeTab.create(menuItem)
        trackTabClicked(newTab)
        tabReselectedListeners[currentTab]?.invoke()
    }

    private fun setExtrasToEmptyIfMalformed(intent: Intent) {
        try {
            intent.getSerializableExtra(AppConstants.BOTTOM_NAV_TAB)
        } catch (e: Exception) {
            Timber.e(e)
            intent.replaceExtras(Bundle())
        }
    }

    private fun setTabSelected(tab: HomeTab) {
        binding.bottomNavigation.menu
            .getItem(tab.position)
            .setChecked(true)
    }

    fun addTabReselectedListener(tab: HomeTab, listener: Function0<Unit>?) {
        tabReselectedListeners[tab] = listener
    }

    private fun onTabSelected(menuItem: MenuItem?): Boolean {
        val newTab: HomeTab = HomeTab.create(menuItem)
        trackTabClicked(newTab)
        return changeTab(newTab)
    }

    private fun changeTab(newTab: HomeTab): Boolean {
        if (isFinishing) return false
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        hideOtherFragments(fragmentTransaction, newTab)
        val existingFragment: Fragment? =
            supportFragmentManager.findFragmentByTag(newTab.toString())
        if (existingFragment == null) {
            val newTabFragment: Fragment = createTabFragment(newTab)
            fragmentTransaction.add(R.id.content, newTabFragment, newTab.toString())
        } else {
            fragmentTransaction.show(existingFragment)
        }
        fragmentTransaction.commitAllowingStateLoss()
        currentTab = newTab
        return true
    }

    private fun createTabFragment(tab: HomeTab): Fragment {
        return when (tab) {
            HomeTab.ALL -> AllVideosFragment.newInstance()
            HomeTab.WEB_SERIES -> WebseriesFragment.newInstance()
            HomeTab.PROFILE -> UserProfileFragment.newInstance()
        }
    }

    private fun hideOtherFragments(fragmentTransaction: FragmentTransaction, newTab: HomeTab) {
        HomeTab.values()
            .filterNot { tab -> tab == newTab }
            .mapNotNull { tab -> supportFragmentManager.findFragmentByTag(tab.toString()) }
            .forEach { fragment: Fragment? -> fragmentTransaction.hide(fragment!!) }
    }

    private val toolbarMenuListener = { menuItem: MenuItem ->
        fun createWebViewIntent(url: String, @StringRes toolbarTitleRes: Int) =
            WebViewActivity.createIntent(this, name = getString(toolbarTitleRes), url = url)

        val intent = when (menuItem.itemId) {
            R.id.open_source_licenses ->
                createWebViewIntent(
                    getString(R.string.licenses_html_path), R.string.open_source_licenses
                )
            R.id.terms_conditions ->
                Intent(Intent.ACTION_VIEW, Uri.parse(HttpConstants.TERMS_CONDITIONS))
            R.id.privacy_policy ->
                Intent(Intent.ACTION_VIEW, Uri.parse(HttpConstants.PRIVACY_POLICY))
            R.id.contact_us_mail -> {
                trackEmailClicked()
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.type = "text/plain"
                intent.data = Uri.parse("mailto:${AppConstants.CONTACT_EMAIL}");
                Intent.createChooser(intent, "Send Email to Nakkalites via")
            }
            else -> null
        }
        try {
            intent?.let(::startActivity)?.let { true } ?: false
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    private fun trackEmailClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.EMAIL_CLICKED)
    }

    private fun trackTabClicked(tab: HomeTab) {
        val eventName: String =
            AnalyticsConstants.Event.TAB_OPENED.formatEn(tab.toString().toLowerCase(Locale.US))
        analyticsManager.logEvent(eventName)
    }

    private fun isFromLauncher(intent: Intent?): Boolean {
        return intent?.categories?.contains(Intent.CATEGORY_LAUNCHER) ?: false
    }
}
