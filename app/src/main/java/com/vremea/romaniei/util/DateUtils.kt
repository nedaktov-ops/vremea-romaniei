package com.vremea.romaniei.util
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
object DateUtils {
    fun formatTime(ts: Long, loc: Locale = Locale.getDefault()) = SimpleDateFormat("HH:mm", loc).format(Date(ts))
    fun formatDate(ts: Long, loc: Locale = Locale.getDefault()) = SimpleDateFormat("d MMM", loc).format(Date(ts))
    fun formatDay(ts: Long, loc: Locale = Locale.getDefault()) = SimpleDateFormat("EEEE", loc).format(Date(ts))
    fun formatHour(ts: Long, loc: Locale = Locale.getDefault()) = SimpleDateFormat("HH", loc).format(Date(ts))
}
