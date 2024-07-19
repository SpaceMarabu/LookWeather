package com.example.weatherapp.presentation.search

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.weatherapp.domain.entity.City
import com.example.weatherapp.presentation.extensions.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchComponentImpl @AssistedInject constructor(
    private val storeFactory: SearchStoreFactory,
    @Assisted private val openReason: OpenReason,
    @Assisted("onBackClicked") private val onBackClicked: () -> Unit,
    @Assisted("onCitySaveToFavourite") private val onCitySaveToFavourite: () -> Unit,
    @Assisted private val onForecastForCityRequested: (City) -> Unit,
    @Assisted componentContext: ComponentContext
) : SearchComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(openReason) }
    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when (it) {
                    SearchStore.Label.ClickBack -> {
                        onBackClicked()
                    }

                    is SearchStore.Label.OpenForecast -> {
                        onForecastForCityRequested(it.city)
                    }

                    SearchStore.Label.SavedToFavourite -> {
                        onCitySaveToFavourite()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<SearchStore.State> = store.stateFlow

    override fun changeSearchQuery(query: String) =
        store.accept(SearchStore.Intent.ChangeSearchQuery(query))

    override fun onClickBack() = store.accept(SearchStore.Intent.ClickBack)

    override fun onClickSearch() = store.accept(SearchStore.Intent.ClickSearch)

    override fun onClickCity(city: City) = store.accept(SearchStore.Intent.ClickCity(city))

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted openReason: OpenReason,
            @Assisted("onBackClicked") onBackClicked: () -> Unit,
            @Assisted("onCitySaveToFavourite") onCitySaveToFavourite: () -> Unit,
            @Assisted onForecastForCityRequested: (City) -> Unit,
            @Assisted componentContext: ComponentContext
        ): SearchComponentImpl
    }

}