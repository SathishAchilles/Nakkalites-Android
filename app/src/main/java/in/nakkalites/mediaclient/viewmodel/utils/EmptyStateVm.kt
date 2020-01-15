package `in`.nakkalites.mediaclient.viewmodel.utils

class EmptyStateVm(layoutId: Int) : DummyVm(layoutId) {
    companion object {
        fun wrapInList(layoutId: Int): List<EmptyStateVm> {
            return listOf(EmptyStateVm(layoutId))
        }
    }
}
