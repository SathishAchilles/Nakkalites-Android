package `in`.nakkalites.mediaclient.view.utils

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out T> {
    val hasValue: Boolean
        get() = when {
            this is Success -> true
            this is Loading -> this.initialValue != null
            this is Error -> this.initialValue != null
            else -> false
        }

    val value: T
        get() = when {
            this is Success -> this.data
            this is Loading -> this.initialValue ?: throw RuntimeException("No data present")
            this is Error -> this.initialValue ?: throw RuntimeException("No data present")
            else -> throw RuntimeException("No data present")
        }

    val unsafeValue: T? get() = if (hasValue) value else null

    class Idle<T> : Result<T>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error<out T>(val initialValue: T? = null, val throwable: Throwable) : Result<T>()
    data class Loading<T>(val initialValue: T? = null) : Result<T>()

    override fun toString(): String {
        return when (this) {
            is Idle -> "Idle"
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[throwable=$throwable]"
            is Loading -> "Loading"
        }
    }
}

/**
 * `true` if [Result] is of type [Success] & holds non-null [Success.data].
 */
val Result<*>.succeeded
    get() = this is Result.Success && data != null

fun <T> idle() = Result.Idle<T>()
