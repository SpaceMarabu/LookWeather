package com.example.weatherapp.presentation.extensions

import android.icu.util.Calendar
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

fun ComponentContext.componentScope() =
    //выполнение немедленно на основном потоке (UI, чтобы предотвратить мерцание)
    // + SupervisorJob предотвращает отмену дочерних корутин, если одна из них упадет
    CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        .apply {
            lifecycle.doOnDestroy { cancel() }
        }

fun Float.tempToFormattedString(): String = "${roundToInt()}°C"

fun Calendar.formattedFullDate(): String {
    val simpleDateFormat = SimpleDateFormat("EEEE | d MMM y", Locale.getDefault())
    return simpleDateFormat.format(this.time)
}

fun Calendar.formattedShorDayOfWeek(): String {
    val simpleDateFormat = SimpleDateFormat("EEE", Locale.getDefault())
    return simpleDateFormat.format(this.time)
}