package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.domain.models.Banner
import `in`.nakkalites.mediaclient.domain.models.Video
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableInt

class BannersVm(banners: List<Banner>) : BaseModel {
    val items = ObservableArrayList<BaseModel>()

    init {
        val list: List<BaseModel> = banners.map { BannerVm(it) }
        items.addAll(list)
    }
}
