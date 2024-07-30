package com.farhanhazmi.yourlocationweather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.location.FusedLocationProviderClient


class MainActivity : AppCompatActivity() {

    private lateinit var locationProvider: LocationProvider
    private lateinit var weatherApiService: WeatherApiService
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiKey = "135555e7dcfdb9b861868233fb5fa2e6"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocationWeather()
        } else {
            // Handle permission denial
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationProvider = LocationProvider(this)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApiService = retrofit.create(WeatherApiService::class.java)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocationWeather()
        }
    }

    private fun getCurrentLocationWeather() {
        locationProvider.getLastLocation { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                weatherApiService.getCurrentWeather(latitude, longitude, apiKey).enqueue(object : Callback<WeatherResponse> {
                    override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                        if (response.isSuccessful) {
                            val weatherResponse = response.body()
                            if (weatherResponse != null) {
                                val weatherDescription = weatherResponse.weather[0].description
                                val temperature = weatherResponse.main.temp
                                val cityName = weatherResponse.name

                                findViewById<TextView>(R.id.textViewWeather).text = "Location: $cityName\nTemperature: $temperature°C\nDescription: $weatherDescription"
                            }
                        }
                    }

                    override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                        findViewById<TextView>(R.id.textViewWeather).text = "Failed to retrieve weather data"
                    }
                })
            } else {
                findViewById<TextView>(R.id.textViewWeather).text = "Failed to get location"
            }
        }
    }
}