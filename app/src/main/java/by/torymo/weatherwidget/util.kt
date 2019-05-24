package by.torymo.weatherwidget

import android.content.Context
import android.preference.PreferenceManager
import by.torymo.weatherwidget.service.CurrentWeatherResponse
import java.text.SimpleDateFormat
import java.util.*

fun getWindDirectionShort(context: Context?, degrees: Float): String {
    if(context == null) return ""

    return when{
        degrees >= 337.5 && degrees < 22.5 -> context.getString(R.string.wind_direction_n)
        degrees >= 22.5 && degrees < 67.5 -> context.getString(R.string.wind_direction_ne)
        degrees >= 67.5 && degrees < 112.5 -> context.getString(R.string.wind_direction_e)
        degrees >= 112.5 && degrees < 157.5 -> context.getString(R.string.wind_direction_se)
        degrees >= 157.5 && degrees < 202.5 -> context.getString(R.string.wind_direction_s)
        degrees >= 202.5 && degrees < 247.5 -> context.getString(R.string.wind_direction_sw)
        degrees >= 247.5 && degrees < 292.5 -> context.getString(R.string.wind_direction_w)
        degrees >= 292.5 && degrees < 337.5 -> context.getString(R.string.wind_direction_nw)
        else -> ""
    }
}

fun getIconResourceForWeatherCondition(weatherId: Int): Int {
    // Based on weather code data found at:
    // https://openweathermap.org/weather-conditions
    // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
    return when(weatherId){
        in 200..232, 761, 781 -> R.drawable.ic_storm
        in 300..321 -> R.drawable.ic_light_rain
        in 500..504, in 520..531 -> R.drawable.ic_rain
        511, in 600..622 -> R.drawable.ic_snow
        in 701..761 -> R.drawable.ic_fog
        800 -> R.drawable.ic_clear_day
        801 -> R.drawable.ic_light_cloud_day
        in 802..804 -> R.drawable.ic_clouds
        else -> -1
    }
}



data class WidgetData(val context: Context?, val wId: Int, val withDefaults: Boolean = false){

    companion object{
        fun saveWidgetData(context: Context, wId: Int, it: CurrentWeatherResponse){
            val wid = wId.toString()
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sp.edit()
            editor.putString(context.getString(R.string.pref_city_name_key) + wid, it.name)
            editor.putInt(context.getString(R.string.pref_weather_icon_key) + wid, it.weather[0].id)
            editor.putFloat(context.getString(R.string.pref_temperature_key) + wid, it.main.temp)
            editor.putFloat(context.getString(R.string.pref_pressure_key) + wid, it.main.pressure)
            editor.putFloat(context.getString(R.string.pref_wind_speed_key) + wid, it.wind.speed)
            editor.putFloat(context.getString(R.string.pref_wind_direction_key) + wid, it.wind.deg)
            editor.putFloat(context.getString(R.string.pref_clouds_key) + wid, it.clouds.all)
            editor.putInt(context.getString(R.string.pref_humidity_key) + wid, it.main.humidity)
            editor.putLong(context.getString(R.string.pref_date_key) + wid, it.dt * 1000L)
            editor.putLong(context.getString(R.string.pref_sunrise_key) + wid, it.sys.sunrise * 1000L)
            editor.putLong(context.getString(R.string.pref_sunset_key) + wid, it.sys.sunset * 1000L)
            editor.putString(context.getString(R.string.pref_weather_description_key) + wid, it.weather[0].description)
            editor.apply()
        }

        fun deleteWidgetData(context: Context, wId: Int){
            val wid = wId.toString()
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sp.edit()
            editor.remove(context.getString(R.string.pref_city_name_key) + wid)
            editor.remove(context.getString(R.string.pref_weather_icon_key) + wid)
            editor.remove(context.getString(R.string.pref_temperature_key) + wid)
            editor.remove(context.getString(R.string.pref_pressure_key) + wid)
            editor.remove(context.getString(R.string.pref_wind_speed_key) + wid)
            editor.remove(context.getString(R.string.pref_wind_direction_key) + wid)
            editor.remove(context.getString(R.string.pref_clouds_key) + wid)
            editor.remove(context.getString(R.string.pref_humidity_key) + wid)
            editor.remove(context.getString(R.string.pref_date_key) + wid)
            editor.remove(context.getString(R.string.pref_sunrise_key) + wid)
            editor.remove(context.getString(R.string.pref_sunset_key) + wid)
            editor.remove(context.getString(R.string.pref_weather_description_key) + wid)

            editor.remove(context.getString(R.string.widget_background_pref_key) + wid)
            editor.remove(context.getString(R.string.widget_transparency_pref_key) + wid)

            editor.apply()
        }
    }

    private val wid = wId.toString()
    private val sp = PreferenceManager.getDefaultSharedPreferences(context)

    val whiteTheme = sp.getBoolean(context?.getString(R.string.widget_background_pref_key) + wid, context?.getString(R.string.widget_background_pref_default)?.toBoolean() ?: false)
    val transparency = sp.getInt(context?.getString(R.string.widget_transparency_pref_key) + wid, context?.getString(R.string.widget_transparency_pref_default)?.toInt() ?: 128)

    val cityName: String = sp.getString(context?.getString(R.string.pref_city_name_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_city)?: "" else "") ?: ""

    val icon = getIconResourceForWeatherCondition(sp.getInt(context?.getString(R.string.pref_weather_icon_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_icon)?.toInt()?:-1 else -1))

    private val temperature = sp.getFloat(context?.getString(R.string.pref_temperature_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_temperature)?.toFloat()?:0.0f else 0.0f)

    private val pressure = sp.getFloat(context?.getString(R.string.pref_pressure_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_pressure)?.toFloat()?:0.0f else 0.0f)

    private val windSpeed =  sp.getFloat(context?.getString(R.string.pref_wind_speed_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_windSpeed)?.toFloat()?:0.0f else 0.0f)

    private val windDirection = sp.getFloat(context?.getString(R.string.pref_wind_direction_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_windDirection)?.toFloat()?:0.0f else 0.0f)

    private val clouds = sp.getFloat(context?.getString(R.string.pref_clouds_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_clouds)?.toFloat()?:0.0f else 0.0f)

    private val humidity = sp.getInt(context?.getString(R.string.pref_humidity_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_humidity)?.toInt()?:0 else 0)

    val description = sp.getString(context?.getString(R.string.pref_weather_description_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_description)?:"" else "")

    private val sunrise = sp.getLong(context?.getString(R.string.pref_sunrise_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_sunrise)?.toLong()?:-1L else -1L)

    private val sunset = sp.getLong(context?.getString(R.string.pref_sunset_key) + wid,
        if(withDefaults) context?.getString(R.string.widget_preview_default_sunset)?.toLong()?:-1L else -1L)

    private val date = sp.getLong(context?.getString(R.string.pref_date_key) + wid,
        if(withDefaults) Date().time else -1)


    fun formattedTemperature(): String{
        if(context == null) return ""
        val temp = context.getString(R.string.format_temperature, Math.round(temperature))
        if(temperature > 0) return String.format("+%s", temp)
        return temp
    }

    fun formattedPressure(): String{
        if(context == null) return ""
        return context.getString(R.string.format_pressure, Math.round(pressure))
    }

    fun formattedWind(): String{
        if(context == null) return ""
        return context.getString(R.string.format_wind, Math.round(windSpeed), getWindDirectionShort(context, windDirection))
    }

    fun formattedCloudiness(): String{
        if(context == null) return ""
        return context.getString(R.string.format_cloudiness, Math.round(clouds))
    }

    fun formattedHumidity(): String{
        if(context == null) return ""
        return context.getString(R.string.format_humidity, humidity)
    }

    fun formattedSunrise(): String{
        if(context == null || sunrise == -1L) return ""
        return context.getString(R.string.format_sunrise,  SimpleDateFormat("HH:mm").format(sunrise))
    }

    fun formattedSunset(): String{
        if(context == null || sunset == -1L) return ""
        return context.getString(R.string.format_sunset,  SimpleDateFormat("HH:mm").format(sunset))
    }

    fun formattedDate(): String{
        if(context == null || date == -1L) return ""
        return context.getString(R.string.format_updated,  SimpleDateFormat("dd MMM, HH:mm").format(date))
    }
}