package com.example.weatherapp.di

import android.content.Context
import com.example.weatherapp.data.local.db.FavouriteCitiesDao
import com.example.weatherapp.data.local.db.FavouriteDB
import com.example.weatherapp.data.network.api.ApiFactory
import com.example.weatherapp.data.network.api.ApiService
import com.example.weatherapp.data.repository.FavouriteRepositoryImpl
import com.example.weatherapp.data.repository.SearchRepositoryImpl
import com.example.weatherapp.data.repository.WeatherRepositoryImpl
import com.example.weatherapp.domain.repository.FavouriteRepository
import com.example.weatherapp.domain.repository.SearchRepository
import com.example.weatherapp.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.Provides


@Module
interface DataModule {

    @[Binds ApplicationScope]
    fun bindFavouriteRepository(impl: FavouriteRepositoryImpl): FavouriteRepository

    @[Binds ApplicationScope]
    fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @[Binds ApplicationScope]
    fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    companion object {

        @[Provides ApplicationScope]
        fun provideApiService(): ApiService = ApiFactory.apiService

        @[Provides ApplicationScope]
        fun provideLocalDb(context: Context): FavouriteDB {
            return FavouriteDB.getInstance(context)
        }

        @[Provides ApplicationScope]
        fun provideFavoriteCitiesDao(database: FavouriteDB): FavouriteCitiesDao {
            return database.favouriteCitiesDao()
        }
    }
}