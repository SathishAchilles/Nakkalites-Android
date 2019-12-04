package `in`.nakkalites.mediaclient.viewmodel.utils

import `in`.nakkalites.mediaclient.view.binding.ProgressBarVm
import `in`.nakkalites.mediaclient.viewmodel.BaseModel
import androidx.databinding.ObservableBoolean
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.disposables.Disposable

object RxTransformers {
    fun <T> dataLoading(items: MutableList<BaseModel>): SingleTransformer<T, T> {
        return dataLoading(false, null, items)
    }

    fun <T> dataLoading(
        isDataLoading: ObservableBoolean?, items: MutableList<BaseModel>
    ): SingleTransformer<T, T> {
        return dataLoading(false, isDataLoading, items)
    }

    fun <T> dataLoading(
        fullScreenLoader: Boolean,
        isDataLoading: ObservableBoolean?,
        items: MutableList<BaseModel>
    ): SingleTransformer<T, T> {
        return SingleTransformer { upstream: Single<T> ->
            upstream
                .doOnSubscribe { disposable: Disposable? ->
                    isDataLoading?.set(true)
                    items.add(ProgressBarVm(fullScreenLoader || items.isEmpty()))
                }
                .doOnEvent { vms: T, e: Throwable? ->
                    items.removeAt(items.size - 1)
                }
                .doOnDispose { items.removeAt(items.size - 1) }
                .doFinally {
                    isDataLoading?.set(false)
                }
        }
    }
}

