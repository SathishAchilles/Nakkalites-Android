package `in`.nakkalites.mediaclient.viewmodel.utils

import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import timber.log.Timber
import java.util.*

fun String.formatEn(vararg args: Any?): String = format(Locale.US, *args)

fun Long.toTimeString(withLiteral: Boolean = false, includeZeros: Boolean = false): String =
    StringBuilder().run {
        val timeUnit = TimeUnit.createTimeUnit(
            this@toTimeString, showHours = false, showMinutes = true, showSeconds = true
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
            if (timeUnit.hours != -1L && (timeUnit.hours > 0 || includeZeros)) append(":")
            if (timeUnit.minutes >= 0) {
                append(timeUnit.minutes.toFormattedTimeString())
                if (withLiteral) append("m")
            }
        }
        if (timeUnit.seconds != -1L) {
            if (timeUnit.minutes != -1L && (timeUnit.minutes > 0 || includeZeros)) append(":")
            if (timeUnit.seconds >= 0) {
                append(timeUnit.seconds.toFormattedTimeString())
                if (withLiteral) append("s")
            }
        }
        this
    }.toString()

typealias ObservableString = NonNullObservableField<String>

fun String.parsePhoneNumber(phoneNumberUtil: PhoneNumberUtil): Phonenumber.PhoneNumber? {
    if (this.isBlank()) return null

    val phoneWithPlus = if (!this.startsWith("+")) "+$this" else this
    return try {
        phoneNumberUtil.parse(phoneWithPlus, null)
    } catch (e: NumberParseException) {
        Timber.e(e)
        null
    }
}

 fun String.toCamelCase(): String {
    var cnt = 0
    val n = length
    val ch = toCharArray()
    var resInd = 0
    for (i in 0 until n) {
        if (ch[i] == ' ') {
            cnt++
            ch[i + 1] = Character.toUpperCase(ch[i + 1])
            continue
        } else ch[resInd++] = ch[i]
    }
    return String(ch, 0, n - cnt)
}
