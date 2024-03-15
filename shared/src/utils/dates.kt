package net.kodein.powerludo.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char


val europeanFormat = LocalDate.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_FULL)
    dayOfMonth()
    chars(" of ")
    monthName(MonthNames.ENGLISH_FULL)
    chars(", ")
    year()
}