package `in`.nakkalites.mediaclient.viewmodel.subscription

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.domain.models.Plan
import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import `in`.nakkalites.mediaclient.viewmodel.utils.DisplayText
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField

class SubscriptionVm(val plan: Plan, currentPlanId: String?) : BaseViewModel() {
    val isSelected = ObservableBoolean(plan.isSelected)
    val id: String = plan.id!!
    val planName = plan.name
    val isCurrentPlan = id == currentPlanId
    val benefits = ObservableArrayList<BenefitVm>()
    val upgradablePrice = ObservableField<DisplayText>()
    val upgradablePlanFrequency = ObservableField<DisplayText>()

    init {
        plan.price?.let {
            upgradablePrice.set(
                DisplayText.Singular(R.string.rupee_x, listOf(plan.price))
            )
        }
        plan.frequency?.let {
            upgradablePlanFrequency.set(
                DisplayText.Singular(R.string.slash_x, listOf(plan.frequency))
            )
        }
        benefits.addAll(plan.descriptions.map { BenefitVm(isSelected, it) })
    }
}

