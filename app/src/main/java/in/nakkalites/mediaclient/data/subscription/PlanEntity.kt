package `in`.nakkalites.mediaclient.data.subscription

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class SubscriptionsResponse(
    @field:Json(name = "current_plan") val currentPlan: PlanEntity?,
    @field:Json(name = "plans") val plans: List<PlanEntity> = listOf(),
)

@JsonClass(generateAdapter = true)
data class PlanEntity(
    @field:Json(name = "uid") val id: String?,
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "price") val price: String?,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "promotion_text") val promotionText: String?,
    @field:Json(name = "frequency") val frequency: String?,
    @field:Json(name = "plan_type") val planType: String?,
    @field:Json(name = "colour_code") val colorCode: String?,
    @field:Json(name = "available_plans_count") val availablePlansCount: Int?,
    @field:Json(name = "content_tags") val contentTags: List<String>?,
)
