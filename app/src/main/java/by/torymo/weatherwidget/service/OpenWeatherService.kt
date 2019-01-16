package by.torymo.weatherwidget.service

import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.Call

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