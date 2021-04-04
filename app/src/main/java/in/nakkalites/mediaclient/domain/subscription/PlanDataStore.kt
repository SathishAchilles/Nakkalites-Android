package `in`.nakkalites.mediaclient.domain.subscription

import `in`.nakkalites.mediaclient.data.PrefsConstants
import `in`.nakkalites.mediaclient.domain.models.Plan
import android.content.SharedPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import timber.log.Timber
import java.lang.reflect.Type

class PlanDataStore(private val prefs: SharedPreferences, private val moshi: Moshi) {

    var plans: List<Plan>? = null
        get() {
            return field ?: getPlanList()
        }
        set(value) {
            field = value
            value?.let { setPlanList(it) }
        }

    private fun setPlanList(user: List<Plan>) {
        val type: Type = Types.newParameterizedType(List::class.java, Plan::class.java)
        val jsonAdapter: JsonAdapter<List<Plan>> = moshi.adapter(type)
        prefs.edit().putString(PrefsConstants.PLAN, jsonAdapter.toJson(user)).apply()
    }

    private fun getPlanList(): List<Plan>? {
        return try {
            val userJson = prefs.getString(PrefsConstants.PLAN, null) ?: return null
            val type: Type = Types.newParameterizedType(List::class.java, Plan::class.java)
            val jsonAdapter: JsonAdapter<List<Plan>> = moshi.adapter(type)
            jsonAdapter.fromJson(userJson)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

}
