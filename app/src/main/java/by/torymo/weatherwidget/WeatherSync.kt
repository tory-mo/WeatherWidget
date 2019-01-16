package by.torymo.weatherwidget

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import by.torymo.weatherwidget.service.CurrentWeatherResponse
import by.torymo.weatherwidget.service.Requester
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.preference.PreferenceManager
import by.torymo.weatherwidget.service.WidgetProvider


class WeatherSyncService: IntentService("WeatherWidget"){

    override fun onHandleIntent(intent: Intent?) {
        val weatherRequester = Requester()
        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        val call = weatherRequester.getCurrentWeatherByCityName("Minsk")

        val ctx = this

        call.enqueue(object: Callback<CurrentWeatherResponse> {
            override fun onResponse(call: Call<CurrentWeatherResponse>, response: Response<CurrentWeatherResponse>) {
                if (response.isSuccessful) {
                    val seriesDetailsResult = response.body()
                    seriesDetailsResult?.let {
                        val editor = sp.edit()
                        editor.putString(getString(R.string.pref_city_name_key), it.name)
                        editor.putString(getString(R.string.pref_weather_base_key), it.base)
                        editor.putLong(getString(R.string.pref_weather_date_key), it.dt)
                        editor.putInt(getString(R.string.pref_weather_icon_key), it.weather[0].id)
                        editor.putFloat(getString(R.string.pref_temperature_key), it.main.temp)
                        editor.putFloat(getString(R.string.pref_pressure_key), it.main.pressure)
                        editor.putFloat(getString(R.string.pref_wind_speed_key), it.wind.speed)
                        editor.putFloat(getString(R.string.pref_wind_direction_key), it.wind.deg)
                        editor.putFloat(getString(R.string.pref_clouds_key), it.clouds.all)
                        editor.putLong(getString(R.string.pref_date_key), it.dt*1000L)
                        editor.apply()

                        notifyWidget(ctx, true)
                    }
                }
            }

            override fun onFailure(call: Call<CurrentWeatherResponse>, t: Throwable) {
            }
        })
    }

    fun notifyWidget(ctx: Context, progressBar: Boolean) {
        val wIds = AppWidgetManager.getInstance(ctx.applicationContext)
            .getAppWidgetIds(ComponentName(ctx.applicationContext, WidgetProvider::class.java))
        if (wIds != null && wIds.isNotEmpty()) {
            val wIntent = Intent(ctx, WidgetProvider::class.java)
            wIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            wIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, wIds)
            if (progressBar)
                wIntent.putExtra(WidgetProvider.PROGRESS_BAR_EXTRA, true)
            ctx.sendBroadcast(wIntent)
        }
    }
}