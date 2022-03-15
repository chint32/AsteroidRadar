package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay

@Dao
interface NasaDao {
    @Query("select * from asteroid_table order by closeApproachDate asc")
    fun getAsteroids(): LiveData<List<Asteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(asteroids: List<Asteroid>)

    @Query("select * from pod_table limit 1")
    fun getPictureOfDay(): LiveData<PictureOfDay>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPictureOfDay(pod: PictureOfDay)
}

@Database(entities = [Asteroid::class, PictureOfDay::class], version = 2, exportSchema = false)
abstract class NasaDatabase : RoomDatabase() {
    abstract val nasaDao: NasaDao
}

private lateinit var INSTANCE: NasaDatabase

fun getDatabase(context: Context): NasaDatabase {
    synchronized(NasaDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                NasaDatabase::class.java,
                "nasa_db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}
