package `in`.nakkalites.mediaclient.data.subscription

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class SubscriptionsResponse(
    @field:Json(name = "config") val configEntity: PlanConfigEntity?,
    @field:Json(name = "upgradeable_plan") val upgradeablePlan: PlanEntity?,
    @field:Json(name = "current_active_plan") val currentPlan: PlanEntity?,
    @field:Json(name = "plans") val plans: List<PlanEntity> = listOf(),
)

@JsonClass(generateAdapter = true)
data class PlanEntity(
    @field:Json(name = "uid") val id: String?,
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "price") val price: String?,
    @field:Json(name = "descriptions") val descriptions: List<String> = listOf(),
    @field:Json(name = "promotion_text") val promotionText: String?,
    @field:Json(name = "frequency") val frequency: String?,
    @field:Json(name = "plan_type") val planType: String?,
    @field:Json(name = "colour_code") val colorCode: String?,
    @field:Json(name = "is_selected") val isSelected: Boolean?,
    @field:Json(name = "available_plans_count") val availablePlansCount: Int?,
    @field:Json(name = "content_tags") val contentTags: List<String>?,
    @field:Json(name = "valid_till") val validTill: String?
)


@JsonClass(generateAdapter = true)
data class PlanConfigEntity(
    @field:Json(name = "thumbnail") val thumbnail: String?
)

@JsonClass(generateAdapter = true)
data class PreSubscriptionResponse(
    @field:Json(name = "id") val id: String?,
    @field:Json(name = "key") val apiKey: String?,
    @field:Json(name = "razorpay_params") val razorpayParams: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class PostSubscriptionResponse(
    @field:Json(name = "error") val error: String?,
    @field:Json(name = "status") val status: String,
)

@JsonClass(generateAdapter = true)
data class FaqResponse(
    @field:Json(name = "faqs") val faqs: List<FaqEntity> = listOf(),
)

@JsonClass(generateAdapter = true)
data class FaqEntity(
    @field:Json(name = "question") val question: String,
    @field:Json(name = "answer") val answer: String,
)



