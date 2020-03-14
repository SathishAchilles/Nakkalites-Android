package `in`.nakkalites.mediaclient.viewmodel.utils

import `in`.nakkalites.mediaclient.viewmodel.BaseModel

open class DummyVm(val layoutId: Int) : BaseModel {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as DummyVm
        return layoutId == that.layoutId
    }

    override fun hashCode(): Int {
        return layoutId
    }

}
