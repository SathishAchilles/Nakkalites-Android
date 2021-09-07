package `in`.nakkalites.mediaclient.domain.subscription

import `in`.nakkalites.mediaclient.data.subscription.SubscriptionService
import `in`.nakkalites.mediaclient.domain.models.Faq
import `in`.nakkalites.mediaclient.domain.models.Plan
import `in`.nakkalites.mediaclient.domain.models.PlanConfig
import io.reactivex.subjects.PublishSubject

class PlanManager(
    private val subscriptionService: SubscriptionService, private val planDataStore: PlanDataStore
) {

    private val planObserver = PublishSubject.create<Boolean>()

    private var currentPlanId: String? = null

    fun getPlanObserver(): PublishSubject<Boolean> = planObserver

    fun getPlans() = subscriptionService.getSubscriptionPlans()
        .map {
            PlanWrapper(
                it.plans.map { planEntity -> Plan.map(planEntity) },
                it.currentPlan?.let { currentPlan -> Plan.map(currentPlan) },
                it.upgradeablePlan?.let { currentPlan -> Plan.map(currentPlan) },
                it.configEntity?.let { planConfigEntity -> PlanConfig.map(planConfigEntity) }
            )
        }
        .doOnSuccess {
            planDataStore.plans = it.plans
            planObserver.onNext(currentPlanId != it.currentPlan?.id)
            currentPlanId = it.currentPlan?.id
        }

    fun getRazorPayParams(planUid: String, currentPlanId: String?) =
        subscriptionService.getRazorPayParams(
            mutableMapOf<String, Any>().apply {
                put("plan_uid", planUid)
                currentPlanId?.let { put("current_plan_uid", currentPlanId) }
            }
        ).map { Triple(it.id, it.razorpayParams, it.apiKey) }

    fun verifyPlan(paymentId: String, orderId: String, signature: String, membershipId: String) =
        subscriptionService.verifyPlan(
            mutableMapOf<String, Any>().apply {
                put("razorpay_payment_id", paymentId)
                put("membership_id", orderId)
                put("razorpay_signature", signature)
                put("membership_id", membershipId)
            }
        ).map { Pair(it.status == "success", it.error) }
            .doOnSuccess {
                planObserver.onNext(it.first)
            }

    fun subscriptionFailure(orderId: String?, code: Int, message: String?) =
        subscriptionService.subscriptionFailure(
            mutableMapOf<String, Any>().apply {
                orderId?.let { put("membership_id", it) }
                put("code", code)
                message?.let { put("message", it) }
            }
        )

    fun getFaqs() = subscriptionService.getFaqs()
        .map { it.faqs.map { Faq.map(it) } }

}

data class PlanWrapper(
    val plans: List<Plan>, val currentPlan: Plan?, val upgradeablePlan: Plan?,
    val planConfig: PlanConfig?
)
