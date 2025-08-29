package me.corv.pillenalarm

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters

/*
Collection of utility methods for LocalDateTime
 */

private val set9pm = TemporalAdjuster {
    it?.with(ChronoField.HOUR_OF_DAY, 21)
        ?.with(ChronoField.MINUTE_OF_HOUR, 0)
        ?.with(ChronoField.SECOND_OF_MINUTE, 0)
        ?.with(ChronoField.MILLI_OF_SECOND, 0)
}

fun in10s(): LocalDateTime {
    return now().plus(Duration.ofSeconds(5))
}

fun in20s(): LocalDateTime {
    return now().plus(Duration.ofSeconds(20))
}

fun in30s(): LocalDateTime {
    return now().plus(Duration.ofSeconds(30))
}

fun in30min(): LocalDateTime {
    return now().plus(Duration.ofMinutes(30))
}

fun in45min(): LocalDateTime {
    return now().plus(Duration.ofMinutes(45))
}

fun in1h(): LocalDateTime {
    return now().plus(Duration.ofHours(1))
}

fun todayAt9pm(): LocalDateTime {
    return now().with(set9pm)
}

fun tomorrowAt9pm(): LocalDateTime {
    return now()
        .plus(Duration.ofDays(1))
        .with(set9pm)
}

fun nextSundayAt9pm(): LocalDateTime {
    return now()
        .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
        .with(set9pm)
}

fun toEpochMilli(time: LocalDateTime): Long {
    val offset = ZoneId.systemDefault().rules.getOffset(time)
    return time.toInstant(offset).toEpochMilli()
}

fun formatDurationAsString(startDateTime: LocalDateTime, endDateTime: LocalDateTime): String {
    val duration = Duration.between(startDateTime, endDateTime)
    val minutes = duration.toMinutes()
    val hours = duration.toHours()
    val days = duration.toDays()

    return when {
        minutes < 1 -> "gleich"
        minutes < 60 -> "in $minutes Minuten"
        days == 0L && hours == 1L -> "in einer Stunde"
        days == 0L && hours < 24 -> "in $hours Stunden"
        days == 1L -> "in einem Tag"
        days > 1L && hours == 0L -> "in $days Tagen"
        else -> "in $days Tagen und ${hours % 24} Stunden"
    }
}

private fun now(): LocalDateTime {
    return LocalDateTime.now(ZoneId.systemDefault())
}