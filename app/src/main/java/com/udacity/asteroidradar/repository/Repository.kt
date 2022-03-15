package com.udacity.asteroidradar.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import androidx.annotation.RequiresApi
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.database.NasaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.google.gson.Gson
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import java.time.LocalDate

class Repository(private val database: NasaDatabase, context: Context) {

    val mcontext = context

    private val MY_API_KEY = "KOVmw7gYWo4dEwSYxlwETW6AELJqnurGahwcojMR"

    val asteroids = database.nasaDao.getAsteroids()
    val pod = database.nasaDao.getPictureOfDay()
    @RequiresApi(Build.VERSION_CODES.O)
    val todaysDate = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    val endDate = todaysDate.plusDays(Constants.DEFAULT_END_DATE_DAYS.toLong())

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun refreshData(){
        withContext(Dispatchers.IO){


            val cm = mcontext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

            if(!isConnected)
                return@withContext

            val asteroidResponse = Network.nasaApi.getAsteroids(
                todaysDate.toString(), endDate.toString(), MY_API_KEY
            ).await()
            val gson = Gson()
            val responseJSONObject = JSONObject(gson.toJson(asteroidResponse))
            val asteroids = parseAsteroidsJsonResult(responseJSONObject)

            val pictureOfDay = Network.nasaApi.getPictureOfDay(MY_API_KEY).await()

            database.nasaDao.insertAll(asteroids)
            database.nasaDao.insertPictureOfDay(pictureOfDay)
        }
    }
}