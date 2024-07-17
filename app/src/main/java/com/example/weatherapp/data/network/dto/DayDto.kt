package com.example.weatherapp.data.network.dto

import com.google.gson.annotations.SerializedName

data class DayDto(
    @SerializedName("date_epoch") val data: Long,
    @SerializedName("day") val dayWeatherDto: DayWeatherDto
)
