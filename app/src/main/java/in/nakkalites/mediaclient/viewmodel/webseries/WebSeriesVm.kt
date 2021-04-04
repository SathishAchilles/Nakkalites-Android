package `in`.nakkalites.mediaclient.viewmodel.webseries

import `in`.nakkalites.mediaclient.domain.models.WebSeries
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import `in`.nakkalites.mediaclient.viewmodel.subscription.PlanUtils
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt

class WebSeriesVm(webSeries: WebSeries) : BaseModel {
    val id = webSeries.id
    val name = webSeries.name
    val thumbnail = webSeries.thumbnailImage
    val planName = webSeries.plan?.name
    val planImg = ObservableField(PlanUtils.getPlanIcon(webSeries.plan))
    val planColorInt = ObservableInt(PlanUtils.getPlanColorInt(webSeries.plan?.colorCode))
}
