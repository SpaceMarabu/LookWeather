package com.example.weatherapp.domain.entity

import android.icu.util.Calendar

data class Weather(
    val tempC: Float,
    val conditionText: String,
    val conditionUrl: String,
    val data: Calendar
)
