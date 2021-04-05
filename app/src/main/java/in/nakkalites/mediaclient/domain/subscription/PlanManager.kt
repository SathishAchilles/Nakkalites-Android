package `in`.nakkalites.mediaclient.domain.subscription

import `in`.nakkalites.mediaclient.data.subscription.SubscriptionService
import `in`.nakkalites.mediaclient.domain.models.Faq
import `in`.nakkalites.mediaclient.domain.models.Plan
import `in`.nakkalites.mediaclient.domain.models.PlanConfig

class PlanManager(
    private val subscriptionService: SubscriptionService, private val planDataStore: PlanDataStore
) {

    fun getPlans() = subscriptionService.getSubscriptionPlans()
        .map {
            PlanWrapper(
                it.plans.map { planEntity -> Plan.map(planEntity) },
                it.currentPlan?.let { currentPlan -> Plan.map(currentPlan) },
                it.upgradeablePlan?.let { currentPlan -> Plan.map(currentPlan) },
                it.configEntity?.let { planConfigEntity -> PlanConfig.map(planConfigEntity) }
            )
        }
        .doOnSuccess { planDataStore.plans = it.plans }

    fun getRazorPayParams(planUid: String) = subscriptionService.getRazorPayParams(
        mutableMapOf<String, Any>("plan_uid" to planUid)
    ).map { Pair(it.razorpayParams, it.apiKey) }

    fun verifyPlan(paymentId: String) = subscriptionService.verifyPlan(
        mutableMapOf<String, Any>("razorpay_payment_id" to paymentId)
    ).map { Pair(it.status == "success", it.error) }

    fun getFaqs() = subscriptionService.getFaqs()
        .map { it.faqs.map { Faq.map(it) } }

}

data class PlanWrapper(
    val plans: List<Plan>, val currentPlan: Plan?, val upgradeablePlan: Plan?,
    val planConfig: PlanConfig?
)
