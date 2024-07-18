package com.example.weatherapp.presentation.favourite

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.domain.usecase.GetCurrentWeatherUseCase
import com.example.weatherapp.domain.usecase.GetFavouriteCitiesUseCase
import com.example.weatherapp.presentation.favourite.FavouriteStore.Intent
import com.example.weatherapp.presentation.favourite.FavouriteStore.Label
import com.example.weatherapp.presentation.favourite.FavouriteStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface FavouriteStore : Store<Intent, State, Label> {

    sealed interface Intent {

        data object ClickSearch : Intent

        data object ClickToFavourite : Intent

        data class CityItemClicked(val city: City) : Intent
    }

    data class State(
        val cityItems: List<CityItem>
    ) {

        data class CityItem(
            val city: City,
            val weatherState: WeatherState
        )

        sealed interface WeatherState {

            data object Initial : WeatherState

            data object Loading : WeatherState

            data object Error : WeatherState

            data class Loaded(
                val tempC: Float,
                val iconUrl: String
            ) : WeatherState
        }
    }

    sealed interface Label {

        data object ClickSearch : Label

        data object ClickToFavourite : Label

        data class CityItemClicked(val city: City) : Label
    }
}

class FavouriteStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val getFavouriteCitiesUseCase: GetFavouriteCitiesUseCase,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase
) {

    fun create(): FavouriteStore =
        object : FavouriteStore, Store<Intent, State, Label> by storeFactory.create(
            name = "FavouriteStore",
            initialState = State(listOf()),
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {

        data class FavouriteCitiesLoaded(val cities: List<City>) : Action
    }

    private sealed interface Msg {

        data class FavouriteCitiesLoaded(val cities: List<City>) : Msg

        data class WeatherLoaded(
            val cityId: Int,
            val tempC: Float,
            val conditionUrl: String
        ) : Msg

        data class WeatherLoadingError(
            val cityId: Int
        ) : Msg

        data class WeatherIsLoading(
            val cityId: Int
        ) : Msg
    }

    private inner class BootstrapperImpl : CoroutineBootstrapper<Action>() {

        override fun invoke() {
            scope.launch {
                getFavouriteCitiesUseCase().collect {
                    dispatch(Action.FavouriteCitiesLoaded(it))
                }
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.CityItemClicked -> {
                    publish(Label.CityItemClicked(intent.city))
                }

                Intent.ClickSearch -> {
                    publish(Label.ClickSearch)
                }

                Intent.ClickToFavourite -> {
                    publish(Label.ClickToFavourite)
                }
            }
        }

        override fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                is Action.FavouriteCitiesLoaded -> {
                    val cities = action.cities
                    dispatch(Msg.FavouriteCitiesLoaded(cities))
                    cities.forEach {
                        scope.launch {
                            loadWeatherForCity(it)
                        }
                    }
                }
            }
        }

        private suspend fun loadWeatherForCity(city: City) {

            dispatch(Msg.WeatherIsLoading(city.id))

            try {
                val weather = getCurrentWeatherUseCase(city.id)
                dispatch(
                    Msg.WeatherLoaded(
                        cityId = city.id,
                        tempC = weather.tempC,
                        conditionUrl = weather.conditionUrl
                    )
                )
            } catch (e: Exception) {
                dispatch(
                    Msg.WeatherLoadingError(city.id)
                )
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(message: Msg): State = when (message) {
            is Msg.FavouriteCitiesLoaded -> {
                this.copy(
                    cityItems = message.cities.map {
                        State.CityItem(
                            city = it,
                            weatherState = State.WeatherState.Initial
                        )
                    }
                )
            }

            is Msg.WeatherIsLoading -> {
                this.copy(
                    cityItems = cityItems.map {
                        if (it.city.id == message.cityId) {
                            it.copy(weatherState = State.WeatherState.Loading)
                        } else {
                            it
                        }
                    }
                )
            }

            is Msg.WeatherLoaded -> {
                this.copy(
                    cityItems = cityItems.map {
                        if (it.city.id == message.cityId) {
                            it.copy(
                                weatherState = State.WeatherState.Loaded(
                                    tempC = message.tempC,
                                    iconUrl = message.conditionUrl
                                )
                            )
                        } else {
                            it
                        }
                    }
                )
            }

            is Msg.WeatherLoadingError -> {
                this.copy(
                    cityItems = cityItems.map {
                        if (it.city.id == message.cityId) {
                            it.copy(
                                weatherState = State.WeatherState.Error
                            )
                        } else {
                            it
                        }
                    }
                )
            }
        }
    }
}
