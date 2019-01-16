package by.torymo.weatherwidget.service

import android.util.Log
import by.torymo.weatherwidget.BuildConfig
import by.torymo.weatherwidget.service.Requester.Companion.APPKEY_PARAM
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import okhttp3.Response
import retrofit2.Call
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory

class Requester {
    private val service: OpenWeatherService

    companion object {

        const val WEATHER_ICON_PATH = "http://openweathermap.org/img/w/" // +01d.png
        const val BASE_URL = "https://api.openweathermap.org"
        const val APPKEY_PARAM = "appid"

        const val UNITS_PARAM = "units"
        const val CITY_NAME_PARAM = "q"
        const val CITY_ID_PARAM = "id"
        const val LAT_PARAM = "lat"
        const val LON_PARAM = "lon"
        const val ZIP_PARAM = "zip"
    }

    init {
        val gson = GsonBuilder()
            .create()
        val client = OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .addInterceptor(ErrorInterceptor())
            .addInterceptor(ApiKeyInterceptor())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
        service = retrofit.create(OpenWeatherService::class.java)
    }

    fun getCurrentWeatherByCityName(city: String): Call<CurrentWeatherResponse>{
        val map = mutableMapOf<String, String>()
        map[CITY_NAME_PARAM] = city
        map[UNITS_PARAM] = "metric"

        return service.getCurrentWeather(map)
    }
}

class ApiKeyInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalR = chain.request()
        val originalH = originalR.url()
        val url = originalH.newBuilder()
            .addQueryParameter(APPKEY_PARAM, BuildConfig.OPEN_WEATHER_API_KEY)
            .build()
        val requestBuilder = originalR.newBuilder().url(url).build()
        return chain.proceed(requestBuilder)
    }
}

class ErrorInterceptor: Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalR = chain.request()
        val response = chain.proceed(originalR)
        return if (response.code() == 200) response
        else{
            Log.e(javaClass.name, response.code().toString())

            response
        }
    }
}