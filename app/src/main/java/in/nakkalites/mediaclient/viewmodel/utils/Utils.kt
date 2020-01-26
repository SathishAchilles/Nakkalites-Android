package `in`.nakkalites.mediaclient.viewmodel.utils

import java.util.*


fun String.beginWithUpperCase(): String {
    return when (this.length) {
        0 -> ""
        1 -> this.toUpperCase()
        else -> this[0].toUpperCase() + this.substring(1)
    }
}

fun String.toCamelCase(): String {
    return this.split('_').joinToString("") {
        it.beginWithUpperCase()
    }
}

fun String.formatEn(vararg args: Any?): String = format(Locale.US, *args)

fun Long.toTimeString(): String = StringBuilder().run {
    val timeUnit = TimeUnit.createTimeUnit(
        this@toTimeString, showHours = true, showMinutes = true, showSeconds = true
    )
    if (timeUnit.hours > 0) {
        append(timeUnit.hours.toFormattedTimeString())
        append("h")
    }
    if (timeUnit.minutes > 0 || timeUnit.hours > 0) {
        if (isNotEmpty()) append(":")
        append(timeUnit.minutes.toFormattedTimeString())
        append("m")
    }
    if (timeUnit.seconds > 0 || timeUnit.minutes > 0 || timeUnit.hours > 0) {
        if (isNotEmpty()) append(":")
        append(timeUnit.seconds.toFormattedTimeString())
        append("s")
    }
    this
}.toString()
