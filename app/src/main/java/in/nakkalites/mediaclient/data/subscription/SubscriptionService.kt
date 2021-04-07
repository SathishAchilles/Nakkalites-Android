package `in`.nakkalites.mediaclient.data.subscription

import `in`.nakkalites.mediaclient.data.HttpConstants
import `in`.nakkalites.mediaclient.data.utils.StringAnyMap
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SubscriptionService {
    @GET(HttpConstants.PLANS)
    fun getSubscriptionPlans(): Single<SubscriptionsResponse>

    @GET(HttpConstants.FAQS)
    fun getFaqs(): Single<FaqResponse>

    @POST(HttpConstants.SUBSCRIPTIONS)
    fun getRazorPayParams(@Body params: StringAnyMap): Single<PreSubscriptionResponse>

    @POST(HttpConstants.PLAN_VERIFY)
    fun verifyPlan(@Body params: StringAnyMap): Single<PostSubscriptionResponse>

    @POST(HttpConstants.PLAN_FAILURE)
    fun subscriptionFailure(@Body params: StringAnyMap): Completable
}
