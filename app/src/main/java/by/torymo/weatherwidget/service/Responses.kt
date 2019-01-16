package by.torymo.weatherwidget.service

import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    val cod: Int = 0,
    val id: Int = 0,
    val name: String = "",
    val dt: Long = 0,
    val base: String = "",
    val coord: Coord = Coord(),
    val weather: List<WeatherCondition> = mutableListOf(),
    val main: MainWeatherInfo = MainWeatherInfo(),
    val wind: Wind = Wind(),
    val clouds: Clouds = Clouds(),
    val rain: Rain = Rain(),
    val sys: Sys = Sys()
)

data class Coord(val lon: Float = 0.0f, val lat: Float = 0.0f)

data class WeatherCondition(
    val id:Int = 0, //weather condition id
    val main: String = "", // Group of weather parameters (Rain, Snow, Extreme etc.)
    val description: String = "", // Weather condition within the group
    val icon: String = "" // Weather icon id
)

data class MainWeatherInfo(
    val temp: Float = 0.0f, // Temperature. Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    val pressure: Float = 0.0f, // Atmospheric pressure (on the sea level, if there is no sea_level or grnd_level data), hPa
    val sea_level: Float = 0.0f, // Atmospheric pressure on the sea level, hPa
    val grnd_level: Float = 0.0f, // Atmospheric pressure on the ground level, hPa
    val humidity: Int = 0, // Humidity, %
    val temp_min: Float = 0.0f, // Minimum temperature at the moment. This is deviation from current temp that is possible for large cities and megalopolises geographically expanded (use these parameter optionally). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    val temp_max: Float = 0.0f // Maximum temperature at the moment. This is deviation from current temp that is possible for large cities and megalopolises geographically expanded (use these parameter optionally). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
)

data class Wind(
    val speed: Float = 0.0f, // Wind speed. Unit Default: meter/sec, Metric: meter/sec, Imperial: miles/hour.
    val deg: Float = 0.0f // Wind direction, degrees (meteorological)
)

data class Clouds(
    val all: Float = 0.0f // Cloudiness, %
)

data class Rain(
    @SerializedName("1h")
    val h1: Float = 0.0f, // Rain volume for the last 1 hour, mm
    @SerializedName("3h")
    val h3: Float = 0.0f // Rain volume for the last 3 hours, mm
)

data class Sys(
    val type: Float = 0.0f, // Internal parameter
    val id:Int = 0, // Internal parameter
    val message: Float = 0.0f, // Internal parameter
    val country: String = "", // Country code (GB, JP etc.)
    val sunrise: Long = 0, // Sunrise time, unix, UTC
    val sunset: Long = 0 // Sunset time, unix, UTC
)