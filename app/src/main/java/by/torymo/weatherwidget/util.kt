package by.torymo.weatherwidget

import android.content.Context
import java.text.SimpleDateFormat

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

fun formattedTemperature(context: Context?, temperature: Float): String{
    if(context == null) return ""
    val temp = context.getString(R.string.format_temperature, Math.round(temperature))
    if(temperature > 0) return String.format("+%s", temp)
    return temp
}

fun formattedPressure(context: Context?, pressure: Float): String{
    if(context == null) return ""
    return context.getString(R.string.format_pressure, pressure)
}

fun formattedWind(context: Context?, windSpeed: Float, windDirection: Float): String{
    if(context == null) return ""
    return context.getString(R.string.format_wind, Math.round(windSpeed), getWindDirectionShort(context, windDirection))
}

fun formattedCloudiness(context: Context?, cloudiness: Float): String{
    if(context == null) return ""
    return context.getString(R.string.format_cloudiness, Math.round(cloudiness))
}

fun formattedDate(date: Long): String{
    if(date == -1L) return ""
    return SimpleDateFormat("dd MMM yyyy HH:mm").format(date)
}