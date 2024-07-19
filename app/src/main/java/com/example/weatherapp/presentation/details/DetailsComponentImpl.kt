package com.example.weatherapp.presentation.details

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

class DetailsComponentImpl @AssistedInject constructor(
    storeFactory: DetailsStoreFactory,
    @Assisted componentContext: ComponentContext,
    @Assisted private val onBackClicked: () -> Unit,
    @Assisted private val city: City
) : DetailsComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(city) }
    private val scope = componentScope()

    init {
        scope.launch {
            store.labels.collect {
                when (it) {
                    DetailsStore.Label.ClickBack -> {
                        onBackClicked()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<DetailsStore.State> = store.stateFlow

    override fun onClickBack() = store.accept(DetailsStore.Intent.CLickBack)

    override fun onClickChangeFavouriteStatus() =
        store.accept(DetailsStore.Intent.ClickChangeFavouriteStatus)

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted componentContext: ComponentContext,
            @Assisted onBackClicked: () -> Unit,
            @Assisted city: City
        ): DetailsComponentImpl
    }
}