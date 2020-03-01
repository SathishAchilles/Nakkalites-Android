package `in`.nakkalites.mediaclient.viewmodel.utils

import java.util.*


fun String.beginWithUpperCase(): String {
    return when (this.length) {
        0 -> ""
        1 -> this.toUpperCase(Locale.getDefault())
        else -> this[0].toUpperCase() + this.substring(1)
    }
}

fun String.toCamelCase(): String {
    return this.split('_').joinToString("") {
        it.beginWithUpperCase()
    }
}

fun String.formatEn(vararg args: Any?): String = format(Locale.US, *args)

fun Long.toTimeString(withLiteral: Boolean = false, includeZeros: Boolean = false): String =
    StringBuilder().run {
        val timeUnit = TimeUnit.createTimeUnit(
            this@toTimeString, showHours = true, showMinutes = true, showSeconds = true
        )
        if (timeUnit.hours != -1L) {
            if (timeUnit.hours > 0) {
                append(timeUnit.hours.toFormattedTimeString())
                if (withLiteral) append("h")
            } else if (includeZeros) {
                append("00")
                if (withLiteral) append("h")
            }
        }
        if (timeUnit.minutes != -1L) {
            if (timeUnit.minutes > 0 || timeUnit.hours > 0) {
                if (timeUnit.hours != -1L && (timeUnit.hours > 0 || includeZeros))
                    append(":")
                append(timeUnit.minutes.toFormattedTimeString())
                if (withLiteral) append("m")
            } else if (includeZeros) {
                if (timeUnit.hours != -1L && (timeUnit.hours > 0 || includeZeros))
                    append(":")
                append("00")
                if (withLiteral) append("m")
            }
        }
        if (timeUnit.seconds != -1L) {
            if (timeUnit.seconds > 0 || timeUnit.minutes > 0 || timeUnit.hours > 0) {
                if (timeUnit.minutes != -1L && (timeUnit.minutes > 0 || includeZeros))
                    append(":")
                append(timeUnit.seconds.toFormattedTimeString())
                if (withLiteral) append("s")
            } else if (includeZeros) {
                if (timeUnit.minutes != -1L && (timeUnit.minutes > 0 || includeZeros))
                    append(":")
                append("00")
                if (withLiteral) append("s")
            }
        }
        this
    }.toString()
