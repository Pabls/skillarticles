package ru.skillbranch.skillarticles.extensions

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

const val SECOND = 1000L
const val MINUTE = 60 * SECOND
const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR

fun Date.format(pattern: String = "HH:mm:ss dd.MM.yy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}

fun Date.shortFormat(): String? {
    val pattern = if (this.isSameDay(Date())) "HH:mm" else "dd.MM.yy"
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}

fun Date.isSameDay(date: Date): Boolean {
    val day1 = this.time / DAY
    val day2 = date.time / DAY
    return day1 == day2
}

fun Date.add(value: Int, units: TimeUnits = TimeUnits.SECOND): Date {
    var time = this.time
    time +=
        when(units) {
            TimeUnits.SECOND -> value * SECOND
            TimeUnits.MINUTE -> value * MINUTE
            TimeUnits.HOUR -> value * HOUR
            TimeUnits.DAY -> value * DAY
        }
    this.time = time
    return this
}

fun Date.humanizeDiff(date: Date = Date()): String {
    val diff = date.time - this.time
    return if (diff >= 0)
        when {
            diff <= 1 * SECOND -> "только что"
            diff <= 45 * SECOND -> "несколько секунд назад"
            diff <= 75 * SECOND -> "минуту назад"
            diff <= 45 * MINUTE -> "${TimeUnits.MINUTE.plural((diff / MINUTE).toInt())} назад"
            diff <= 75 * MINUTE -> "час назад"
            diff <= 22 * HOUR -> "${TimeUnits.HOUR.plural((diff / HOUR).toInt())} назад"
            diff <= 26 * HOUR -> "день назад"
            diff <= 360 * DAY -> "${TimeUnits.HOUR.plural((diff / DAY).toInt())} назад"
            else -> "более года назад"
        }
    else {
        when {
            diff.absoluteValue >= 360 * DAY -> "более чем через год"
            diff.absoluteValue >= 26 * HOUR -> "через ${TimeUnits.DAY.plural((diff.absoluteValue / DAY).toInt())}"
            diff.absoluteValue >= 22 * HOUR -> "через день"
            diff.absoluteValue >= 75 * MINUTE -> "через ${TimeUnits.HOUR.plural((diff.absoluteValue / HOUR).toInt())}"
            diff.absoluteValue >= 45 * MINUTE -> "через час"
            diff.absoluteValue >= 75 * SECOND -> "через ${TimeUnits.MINUTE.plural((diff.absoluteValue / MINUTE).toInt())}"
            diff.absoluteValue >= 45 * SECOND -> "через минуту"
            else -> "через несколько секунд"
        }
    }
}

private fun pluralsNumber(value: Int, number: TimeUnits) = when (number) {
    TimeUnits.SECOND ->
        if (value in 11..19) {
            "секунд"
        } else {
            when (value % 10) {
                1 -> "секунду"
                2, 3, 4 -> "секунды"
                else -> "секунд"
            }
        }
    TimeUnits.MINUTE ->
        if (value in 11..19) {
            "минут"
        } else {
            when (value % 10) {
                1 -> "минуту"
                2, 3, 4 -> "минуты"
                else -> "минут"
            }
        }
    TimeUnits.HOUR ->
        if (value in 11..19) {
            "часов"
        } else {
            when (value % 10) {
                1 -> "час"
                2, 3, 4 -> "часа"
                else -> "часов"
            }
        }
    TimeUnits.DAY ->
        if (value in 11..19) {
            "дней"
        } else {
            when (value % 10) {
                1 -> "день"
                2, 3, 4 -> "дня"
                else -> "дней"
            }
        }
}

enum class TimeUnits {
    SECOND,
    MINUTE,
    HOUR,
    DAY;

    fun plural(value: Int) =
        when (this) {
            SECOND -> "$value ${pluralsNumber(value, SECOND)}"
            MINUTE -> "$value ${pluralsNumber(value, MINUTE)}"
            HOUR -> "$value ${pluralsNumber(value, HOUR)}"
            DAY -> "$value ${pluralsNumber(value, DAY)}"
        }
}