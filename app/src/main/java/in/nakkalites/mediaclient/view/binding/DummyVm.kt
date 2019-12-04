package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.viewmodel.BaseModel

class DummyVm(val layoutId: Int) : BaseModel {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DummyVm
        return layoutId == that.layoutId
    }

    override fun hashCode(): Int {
        return layoutId
    }

}
