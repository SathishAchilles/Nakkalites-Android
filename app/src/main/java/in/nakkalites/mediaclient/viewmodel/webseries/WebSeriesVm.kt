package `in`.nakkalites.mediaclient.viewmodel.webseries

import `in`.nakkalites.mediaclient.domain.models.WebSeries
import `in`.nakkalites.mediaclient.viewmodel.BaseModel

class WebSeriesVm(webSeries: WebSeries) : BaseModel {
    val id = webSeries.id
    val name = webSeries.name
    val thumbnail = webSeries.thumbnailImage
}
