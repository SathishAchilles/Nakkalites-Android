package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.domain.models.WebSeries
import `in`.nakkalites.mediaclient.viewmodel.BaseModel

class WebSeriesVm(webSeries: WebSeries) : BaseModel{
    val name = webSeries.name
    val thumbnail = webSeries.thumbnailImage
}
