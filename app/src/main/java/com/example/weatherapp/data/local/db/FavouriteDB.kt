package com.example.weatherapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherapp.data.local.model.CityDbModel

@Database(entities = [CityDbModel::class], version = 1, exportSchema = false)
abstract class FavouriteDB : RoomDatabase() {

    abstract fun favouriteCitiesDao(): FavouriteCitiesDao

    companion object {

        private var INSTANCE: FavouriteDB? = null
        private val LOCK = Any()
        private const val DB_NAME = "FavouriteDB"

        fun getInstance(context: Context): FavouriteDB {

            INSTANCE?.let { return it }

            synchronized(LOCK) {
                INSTANCE?.let { return it }

                val database = Room.databaseBuilder(
                    context = context,
                    klass = FavouriteDB::class.java,
                    name = DB_NAME
                ).build()
                INSTANCE = database
                return database
            }
        }
    }
}