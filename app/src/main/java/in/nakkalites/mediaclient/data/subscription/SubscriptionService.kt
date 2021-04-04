package `in`.nakkalites.mediaclient.data.subscription

import `in`.nakkalites.mediaclient.data.HttpConstants
import io.reactivex.Single
import retrofit2.http.GET

interface SubscriptionService {
    @GET(HttpConstants.SUBSCRIPTIONS)
    fun getSubscriptionPlans(): Single<SubscriptionsResponse>
}
