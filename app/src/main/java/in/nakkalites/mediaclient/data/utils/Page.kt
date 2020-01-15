package `in`.nakkalites.mediaclient.data.utils

import com.squareup.moshi.Json

/**
 * Represents a response page in paged APIs.
 */
abstract class Page(
    @field:Json(name = "cursor") val cursor: String? = null
) {
    abstract val pageSize: Int
}
