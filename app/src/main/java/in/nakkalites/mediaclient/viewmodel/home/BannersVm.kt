package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.logging.loge
import `in`.nakkalites.mediaclient.domain.models.Banner
import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableInt

class BannersVm(banners: List<Banner>) : BaseModel {
    val items = ObservableArrayList<BaseModel>()

    init {
        val list = banners.map {
            //            loge("" + (it.video?.toString() ?: "Empty"))
//            loge("" + (it.webSeries?.toString() ?: "Empty"))
            BannerVm(it)
        }
//        loge("size ${list.size} ${list.map { it.thumbnail ?: "Null" }.toString()}")
        items.addAll(list)
    }

    override fun toString(): String {
        return "BannersVm(items=$items)"
    }

}
