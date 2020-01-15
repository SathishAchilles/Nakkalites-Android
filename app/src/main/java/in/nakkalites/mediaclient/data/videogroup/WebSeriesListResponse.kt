package `in`.nakkalites.mediaclient.data.videogroup

import `in`.nakkalites.mediaclient.data.utils.Page
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WebSeriesListResponse(
    @field:Json(name = "web_series_list") val webSeriesList: List<WebSeriesEntity>
) : Page() {
    override val pageSize = webSeriesList.size
}
