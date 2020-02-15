package `in`.nakkalites.mediaclient.viewmodel.utils

import com.kizitonwose.time.*
import kotlin.math.floor

data class TimeUnit(
    val totalMillis: Long, val days: Long, val hours: Long, val minutes: Long, val seconds: Long,
    val showDays: Boolean, val showHours: Boolean, val showMinutes: Boolean,
    val showSeconds: Boolean
) {
    companion object {
        fun createTimeUnit(
            totalMillis: Long, showDays: Boolean = false, showHours: Boolean = false,
            showMinutes: Boolean = false, showSeconds: Boolean = false
        ): TimeUnit {
            val days = if (showDays) {
                totalMillis.milliseconds.inDays.floor
            } else {
                -1
            }
            val hours = when {
                showDays -> totalMillis.milliseconds.inDays.value.fraction.days.inHours.floor
                showHours -> totalMillis.milliseconds.inHours.floor
                else -> -1
            }
            val minutes = when {
                showHours -> totalMillis.milliseconds.inHours.value.fraction.hours.inMinutes.floor
                showMinutes -> totalMillis.milliseconds.inMinutes.floor
                else -> -1
            }
            val seconds = when {
                showMinutes -> {
                    totalMillis.milliseconds.inMinutes.value.fraction.minutes.inSeconds.floor
                }
                showSeconds -> totalMillis.milliseconds.inSeconds.floor
                else -> -1
            }
            return TimeUnit(
                totalMillis, days, hours, minutes, seconds, showDays, showHours, showMinutes,
                showSeconds
            )
        }
    }

    override fun toString(): String {
        return "TimeUnit(totalMillis=$totalMillis, days=$days, hours=$hours, minutes=$minutes, " +
                "seconds=$seconds, showDays=$showDays, showHours=$showHours, " +
                "showMinutes=$showMinutes, showSeconds=$showSeconds)"
    }
}

fun Long.toFormattedTimeString(): String =
    when {
        this > 10 -> this.toString()
        this > 0 -> "%02d".formatEn(this)
        else -> "00"
    }

private val Interval<com.kizitonwose.time.TimeUnit>.floor: Long
    get() = floor(this.value).toLong()

private val Double.fraction: Double
    get() = this - floor(this)
