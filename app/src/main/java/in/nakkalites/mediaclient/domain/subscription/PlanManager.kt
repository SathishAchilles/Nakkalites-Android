package `in`.nakkalites.mediaclient.domain.subscription

import `in`.nakkalites.mediaclient.data.subscription.SubscriptionService
import `in`.nakkalites.mediaclient.domain.models.Plan

class PlanManager(
    private val subscriptionService: SubscriptionService, private val planDataStore: PlanDataStore
) {

    fun getPlans() = subscriptionService.getSubscriptionPlans()
        .map {
            Pair(
                it.currentPlan?.let { currentPlan -> Plan.map(currentPlan) },
                it.plans.map { planEntity -> Plan.map(planEntity) })
        }
        .doOnSuccess { planDataStore.plans = it.second }
}
