package `in`.nakkalites.mediaclient.viewmodel.home

import `in`.nakkalites.mediaclient.domain.models.Banner
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.databinding.ObservableArrayList

class BannersVm(banners: List<Banner>) : BaseModel {
    val items = ObservableArrayList<BaseModel>()

    init {
        items.addAll(banners.map { BannerVm(it) })
    }

    override fun toString(): String {
        return "BannersVm(items=$items)"
    }

}
