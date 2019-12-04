package `in`.nakkalites.mediaclient.domain.utils

/**
 * Used in conjunction with RecyclerViewScrollPager and PagingBody.
 */
interface PagingCallback {

    fun onFinished()

    fun onReset()
}
