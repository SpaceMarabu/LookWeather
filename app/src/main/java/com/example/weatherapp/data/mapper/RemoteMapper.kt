package com.example.weatherapp.data.mapper

import android.icu.util.Calendar
import com.example.weatherapp.data.network.dto.CityDto
import com.example.weatherapp.data.network.dto.ForecastDto
import com.example.weatherapp.data.network.dto.WeatherCurrentDto
import com.example.weatherapp.data.network.dto.WeatherDto
import com.example.weatherapp.data.network.dto.WeatherForecastDto
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.entity.Forecast
import com.example.weatherapp.domain.entity.Weather
import java.util.Date

fun CityDto.toEntity() = City(id, name, country)

fun List<CityDto>.toEntities() = map { it.toEntity() }

fun WeatherForecastDto.toEntity() = Forecast(
    currentWeather = current.toEntity(),
    upcoming = forecast.forecastDay.drop(1).map { dayDto ->
        val dayWeatherDto = dayDto.dayWeatherDto
        Weather(
            tempC = dayWeatherDto.tempC,
            conditionText = dayWeatherDto.conditionDto.text,
            conditionUrl = dayWeatherDto.conditionDto.iconUrl.correctImageUrl(),
            date = dayDto.date.toCalendar()
        )
    }
)

fun WeatherCurrentDto.toEntity(): Weather = this.toEntity()

fun WeatherDto.toEntity() = Weather(
    tempC = tempC,
    conditionText = conditionDto.text,
    conditionUrl = conditionDto.iconUrl.correctImageUrl(),
    date = date.toCalendar()
)

private fun Long.toCalendar() = Calendar.getInstance().apply {
    time = Date(this@toCalendar * 1000)
}

private fun String.correctImageUrl() =
    "https:$this".replace(oldValue = "64х64", newValue = "128х128")

