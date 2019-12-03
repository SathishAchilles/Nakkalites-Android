package `in`.nakkalites.mediaclient.view.binding

import `in`.nakkalites.mediaclient.viewmodel.BaseViewModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.ObservableList.OnListChangedCallback
import androidx.databinding.ViewDataBinding
import androidx.viewpager.widget.PagerAdapter


class BindingPagerAdapter<T : BaseViewModel>(
    private val items: ObservableList<T>?, private val viewProvider: ViewProvider,
    private val viewModelBinder: ViewModelBinder
) : PagerAdapter() {
    private var pageTitles: PageTitles<T>? = null
    private val callback: PagerObservableListCallback<T>

    interface PageTitles<T> {
        fun getPageTitle(position: Int, item: T): CharSequence?
    }

    fun setPageTitles(pageTitles: PageTitles<T>?) {
        this.pageTitles = pageTitles
    }

    override fun getCount(): Int {
        return items?.size ?: 0
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val item = items!![position]
        return pageTitles?.getPageTitle(position, item)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = items!![position]
        val itemLayout: Int = viewProvider.getView(item)
        val inflater = LayoutInflater.from(container.context)
        val itemBinding =
            DataBindingUtil.inflate<ViewDataBinding>(inflater, itemLayout, container, false)
        viewModelBinder.bind(itemBinding, item)
        itemBinding.executePendingBindings()
        val root = itemBinding.root
        container.addView(root)
        root.tag = item
        return root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any) = view === `object`

    private class PagerObservableListCallback<T> internal constructor(private val adapter: PagerAdapter) :
        OnListChangedCallback<ObservableList<T>?>() {
        override fun onChanged(sender: ObservableList<T>?) {
            adapter.notifyDataSetChanged()
        }

        override fun onItemRangeChanged(
            sender: ObservableList<T>?, positionStart: Int, itemCount: Int
        ) {
            onChanged(sender)
        }

        override fun onItemRangeInserted(
            sender: ObservableList<T>?, positionStart: Int, itemCount: Int
        ) {
            onChanged(sender)
        }

        override fun onItemRangeMoved(
            sender: ObservableList<T>?, fromPosition: Int, toPosition: Int, itemCount: Int
        ) {
            onChanged(sender)
        }

        override fun onItemRangeRemoved(
            sender: ObservableList<T>?, positionStart: Int, itemCount: Int
        ) {
            onChanged(sender)
        }
    }

    init {
        callback = PagerObservableListCallback(this)
        items?.addOnListChangedCallback(callback)
    }
}
