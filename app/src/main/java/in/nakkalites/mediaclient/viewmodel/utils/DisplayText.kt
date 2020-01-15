package `in`.nakkalites.mediaclient.viewmodel.utils


import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * Model for strings resources in Android. To be used in classes like ViewModels which should not
 * reference platform-specific APIs.
 *
 * Supports nesting string resources inside string resources recursively.
 *
 * See https://github.com/googlesamples/android-architecture/pull/635.
 */
sealed class DisplayText {

    data class Plain(val text: String) : DisplayText()

    data class Singular(
        @StringRes val resource: Int,
        val formatArgs: List<Any> = emptyList()
    ) : DisplayText() {
        val quantity: Int = 1
    }

    data class Plural(
        @PluralsRes val resource: Int,
        val quantity: Int,
        val formatArgs: List<Any> = emptyList()
    ) : DisplayText()

    object Empty : DisplayText()

    fun getText(resources: Resources?): String {
        return when (this) {
            is Empty -> ""
            else -> resources?.let { res -> resolveText(res, this) } ?: ""
        }
    }
}

fun resolveText(res: Resources, any: Any): String = with(any) {
    when (this) {
        is DisplayText.Plain -> text
        is DisplayText.Singular -> {
            if (formatArgs.isEmpty()) { // No array/list allocation in the most common case.
                res.getString(resource)
            } else {
                val formatArgsArray = formatArgs.map { resolveText(res, it) }.toTypedArray()
                res.getString(resource, *formatArgsArray)
            }
        }
        is DisplayText.Plural -> {
            if (formatArgs.isEmpty()) {
                res.getQuantityString(resource, quantity)
            } else {
                val formatArgsArray = formatArgs.map { resolveText(res, it) }.toTypedArray()
                res.getQuantityString(resource, quantity, *formatArgsArray)
            }
        }
        else -> any.toString()
    }
}
