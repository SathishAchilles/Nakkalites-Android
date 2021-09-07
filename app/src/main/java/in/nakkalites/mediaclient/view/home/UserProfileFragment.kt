package `in`.nakkalites.mediaclient.view.home

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AnalyticsConstants
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.app.manager.AnalyticsManager
import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.databinding.FragmentUserProfileBinding
import `in`.nakkalites.mediaclient.domain.utils.errorHandler
import `in`.nakkalites.mediaclient.view.BaseFragment
import `in`.nakkalites.mediaclient.view.profile.ProfileEditActivity
import `in`.nakkalites.mediaclient.view.subscription.FaqActivity
import `in`.nakkalites.mediaclient.view.subscription.ManageSubscriptionActivity
import `in`.nakkalites.mediaclient.view.subscription.SubscriptionsActivity
import `in`.nakkalites.mediaclient.view.utils.EventObserver
import `in`.nakkalites.mediaclient.view.utils.Result
import `in`.nakkalites.mediaclient.view.utils.playStoreUrl
import `in`.nakkalites.mediaclient.view.utils.shareTextIntent
import `in`.nakkalites.mediaclient.view.webview.WebViewActivity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import org.koin.android.ext.android.inject

class UserProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentUserProfileBinding
    private val vm by inject<UserProfileVm>()
    private val analyticsManager by inject<AnalyticsManager>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater)
        binding.vm = vm
        binding.callbacks = callbacks
        vm.fetchProfile()
        vm.viewStates().observe(requireActivity(), EventObserver {
            when (it) {
                is Result.Success -> {
                    binding.mainLayout.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
                is Result.Error -> {
                    requireActivity().errorHandler(it.throwable)
                }
                else -> showLoading()
            }
        })
        return binding.root
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private val callbacks = object : UserProfileCallbacks {
        override fun onLicencesClick() {
            createWebViewIntent(
                getString(R.string.licenses_html_path), R.string.open_source_licenses
            ).let(::startActivity)
        }

        override fun onTnCClick() {
            Intent(
                Intent.ACTION_VIEW, Uri.parse(HttpConstants.TERMS_CONDITIONS)
            ).let(::startActivity)
        }

        override fun onPrivacyPolicyClick() {
            Intent(
                Intent.ACTION_VIEW, Uri.parse(HttpConstants.PRIVACY_POLICY)
            ).let(::startActivity)
        }

        override fun onContactUsClick() {
            trackEmailClicked()
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.type = "text/plain"
            intent.data = Uri.parse("mailto:${AppConstants.CONTACT_EMAIL}");
            Intent.createChooser(intent, "Send Email to Nakkalites via").let(::startActivity)
        }

        override fun onShareClick() {
            trackShareClicked()
            val intent = shareTextIntent(
                getString(R.string.share_nakkalites),
                getString(R.string.share_text, playStoreUrl())
            )
            startActivity(intent)
        }

        override fun onFaqClick() {
            trackFaqClicked()
            startActivity(FaqActivity.createIntent(requireActivity()))
        }

        override fun onManageSubscriptionClick() {
            trackManageSubscriptionClicked()
            startActivity(ManageSubscriptionActivity.createIntent(requireActivity()))
        }

        override fun onEditClick() {
            trackEditClicked()
            startActivity(ProfileEditActivity.createIntent(requireActivity()))
        }

        override fun onPlanCTAClick() {
            trackProfilePlanCTAClicked()
            startActivity(SubscriptionsActivity.createIntent(requireActivity()))
        }

    }

    private fun createWebViewIntent(url: String, @StringRes toolbarTitleRes: Int) =
        WebViewActivity.createIntent(requireContext(), name = getString(toolbarTitleRes), url = url)

    private fun trackEmailClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.EMAIL_CLICKED)
    }

    private fun trackShareClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.SHARE_CLICKED)
    }

    private fun trackFaqClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.FAQ_CLICKED)
    }

    private fun trackEditClicked() {
        analyticsManager.logEvent(AnalyticsConstants.Event.PROFILE_EDIT_CLICKED)
    }

    private fun trackProfilePlanCTAClicked() {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.CURRENT_PLAN, vm.planName.get())
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.PROFILE_PLAN_CTA_CLICKED, bundle)
    }

    private fun trackManageSubscriptionClicked() {
        val bundle = Bundle().apply {
            putString(AnalyticsConstants.Property.CURRENT_PLAN, vm.planName.get())
        }
        analyticsManager.logEvent(AnalyticsConstants.Event.MANAGE_SUBSCRIPTION_CLICKED, bundle)
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserProfileFragment()
    }
}

interface UserProfileCallbacks {
    fun onLicencesClick()
    fun onTnCClick()
    fun onPrivacyPolicyClick()
    fun onContactUsClick()
    fun onShareClick()
    fun onFaqClick()
    fun onManageSubscriptionClick()
    fun onEditClick()
    fun onPlanCTAClick()
}
