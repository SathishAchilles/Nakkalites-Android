package `in`.nakkalites.mediaclient.viewmodel.subscription

import `in`.nakkalites.mediaclient.R
import `in`.nakkalites.mediaclient.app.constants.AppConstants
import `in`.nakkalites.mediaclient.domain.models.Plan
import android.graphics.Color

object PlanUtils {

    fun getPlanColorInt(colorCode: String?): Int {
        return try {
            Color.parseColor(colorCode)
        } catch (e: Exception) {
            Color.parseColor(AppConstants.DEFAULT_TAG_BG_COLOR)
        }
    }

    fun getPlanIcon(plan: Plan?): Int? {
        return plan?.let {
            when (it.planType) {
                "SUPER_PRIME" -> {
                    R.drawable.ic_crown
                }
                "PRIME" -> {
                    R.drawable.ic_star
                }
                else -> null
            }
        }
    }
}
