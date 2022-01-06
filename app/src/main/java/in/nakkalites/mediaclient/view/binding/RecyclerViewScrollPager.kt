package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.domain.utils.PagingCallback
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.recyclerview.RecyclerViewScrollEvent
import com.jakewharton.rxbinding3.recyclerview.scrollEvents
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


class RecyclerViewScrollPager(
    lifecycleOwner: LifecycleOwner, private val recyclerViewSupplier: () -> RecyclerView,
    private val loadNextPage: Runnable, private val isDataLoading: () -> Boolean,
    private val autoAttachScrollEvent: Boolean = true
) : LifecycleObserver {
    private var disposable: Disposable? = null
    private var hasPagingFinished = false
    val pagingCallback: PagingCallback = object :
        PagingCallback {
        override fun onFinished() {
            hasPagingFinished = true
        }

        override fun onReset() {
            hasPagingFinished = false
        }
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    private fun observeScrollEvents(view: RecyclerView) {
        if (disposable != null) return  // Already observing.

        val layoutManager = view.layoutManager as LinearLayoutManager
        disposable = view.scrollEvents()
            .debounce(PAGINATION_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { event ->
                !isDataLoading() && !hasPagingFinished
                        && isScrollingTowardsEnd(event, layoutManager)
            }
            .subscribe { loadNextPage.run() }
    }

    fun attachScrollEvent() {
        observeScrollEvents(recyclerViewSupplier())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun autoAttachScrollEvent() {
        if (autoAttachScrollEvent) attachScrollEvent()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dispose() {
        disposable?.dispose()
        disposable = null
    }

    private fun isScrollingTowardsEnd(
        event: RecyclerViewScrollEvent, layoutManager: LinearLayoutManager
    ): Boolean {
        if (scrollDelta(layoutManager, event) <= 0) return false

        val totalItemsCount = layoutManager.itemCount
        val visibleItemsCount = event.view.childCount
        val firstVisibleItemIndex = layoutManager.findFirstVisibleItemPosition()
        return totalItemsCount - visibleItemsCount <= firstVisibleItemIndex + OFF_SCREEN_ITEMS_COUNT
    }

    private fun scrollDelta(layoutManager: LinearLayoutManager, event: RecyclerViewScrollEvent) =
        if (layoutManager.orientation == LinearLayout.HORIZONTAL) event.dx else event.dy

    companion object {
        private const val OFF_SCREEN_ITEMS_COUNT = 4
        private const val PAGINATION_DEBOUNCE_TIMEOUT = 200
    }
}
