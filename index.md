For this project we need an API key for [openweathermap.org](https://openweathermap.org)

#Preparing

###Retrofit
```markdown
def retrofitVersion = '2.4.0'
implementation "com.squareup.retrofit2:retrofit:$retrofitVersion"
implementation "com.squareup.retrofit2:converter-gson:$retrofitVersion"
implementation "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2"
```
###okhttp3
```markdown
def okhttpVersion = '3.9.1'
implementation "com.squareup.okhttp3:okhttp:$okhttpVersion"
implementation "com.squareup.okhttp3:logging-interceptor:$okhttpVersion"
```
Classes to handle JSON Responses automatically with Gson
```markdown
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
```
Interface describing requests to Retrofit
```markdown
interface OpenWeatherService{

    /*
    * Current weather
    * https://api.openweathermap.org/data/2.5/weather?appid=<<api_key>>&units={metric/imperial}
    *
    * &q={city name},{country code} - by city name
    * &id={city id} - by city id
    * &lat={lat}&lon={lon} - by coords
    * &zip={zip code},{country code} - by zip code
    * {
        "cod":200
        "id":2172797,
        "name":"Cairns",
        "dt":1435658272,
        "coord":{
            "lon":145.77,"lat":-16.92
         },
        "weather":[{"id":803,"main":"Clouds","description":"broken clouds","icon":"04n"}],
        "base":"cmc stations",
        "main":{"temp":293.25,"pressure":1019,"humidity":83,"temp_min":289.82,"temp_max":295.37},
        "wind":{"speed":5.1,"deg":150},
        "clouds":{"all":75},
        "rain":{"3h":3},
        "sys":{"type":1,"id":8166,"message":0.0166,"country":"AU","sunrise":1435610796,"sunset":1435650870},
      }
    */
    @GET("/data/2.5/weather")
    fun getCurrentWeather(@QueryMap map: Map<String, String>): Call<CurrentWeatherResponse>
}
```

Classes to initialize Retrofit and make requests
```markdown
class Requester {
    private val service: OpenWeatherService

    companion object {
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
```
#Widget

Step 1: layout
Step 2: WidgetProvider class
