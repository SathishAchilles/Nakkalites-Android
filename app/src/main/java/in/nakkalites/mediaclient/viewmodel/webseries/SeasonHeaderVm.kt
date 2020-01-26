package `in`.nakkalites.mediaclient.viewmodel.webseries

import `in`.nakkalites.mediaclient.domain.models.Season
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.lifecycle.MutableLiveData

class SeasonHeaderVm(private val seasons: List<Season>) : BaseModel {
    val seasonsEntries: List<Pair<String, String>> = seasons.map { it.id to it.name }
    val seasonIdItemPosition = MutableLiveData<Int>()
    var seasonIdValue
        get() =
            seasonIdItemPosition.value?.let {
                seasons[it].id
            }
        set(value) {
            val position = seasons.indexOfFirst {
                it.id == value
            }
            if (position != -1) {
                seasonIdItemPosition.value = position
            }
        }
    val seasonIdItem
        get() =
            seasonIdItemPosition.value?.let {
                seasons[it]
            }

}
