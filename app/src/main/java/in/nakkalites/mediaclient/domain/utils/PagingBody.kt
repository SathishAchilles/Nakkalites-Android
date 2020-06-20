package `in`.nakkalites.mediaclient.domain.utils

import `in`.nakkalites.mediaclient.app.constants.AppConstants.PAGE_SIZE
import `in`.nakkalites.mediaclient.data.utils.Page
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference


/**
 * Builds request bodies ({@code Map<String, Object>}) for paged APIs and notifies when paging is finished.
 * This class is thread-safe.
 */
class PagingBody(
    offset: Int = 1, cursor: String? = null, private val limit: Int = PAGE_SIZE,
    private val pagingCallback: PagingCallback?
) {
    private val offsetAtomicInt: AtomicInteger = AtomicInteger(offset)
    private val cursorAtomicString: AtomicReference<String?> = AtomicReference(cursor)
    private val hasPagingFinished: AtomicBoolean = AtomicBoolean(false)

    fun getCursor(): String? {
        return cursorAtomicString.get()
    }

    fun reset() {
        offsetAtomicInt.set(1)
        cursorAtomicString.set(null)
        hasPagingFinished.set(false)
        pagingCallback?.onReset()
    }

    fun onNextPage(pageSize: Int, cursor: String?) {
        hasPagingFinished.set(limit > pageSize)
        if (pagingCallback != null && hasPagingFinished.get()) {
            pagingCallback.onFinished()
        }
        val newOffset = if (hasPagingFinished.get()) 1 else offsetAtomicInt.get() + pageSize
        this.offsetAtomicInt.set(newOffset)
        this.cursorAtomicString.set(cursor)
    }

    fun onNextPage(page: Page) {
        val pageSize = page.pageSize
        val cursor = page.cursor
        onNextPage(pageSize, cursor)
    }


    fun toMap(): Map<String, Any> {
        return mutableMapOf<String, Any>()
            .apply {
                put("offset", offsetAtomicInt.get())
                put("limit", limit)
                cursorAtomicString.get()?.also { put("cursor", it) }
            }.toMap()
    }

    fun isFirstPage(): Boolean {
        return offsetAtomicInt.get() == 1
    }

    fun hasPagingFinished(): Boolean {
        return hasPagingFinished.get()
    }
}
