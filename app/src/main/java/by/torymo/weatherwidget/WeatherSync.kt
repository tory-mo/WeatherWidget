package by.torymo.weatherwidget

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import by.torymo.weatherwidget.service.*


class WeatherSyncService: IntentService("WeatherWidget"){

    companion object{
        private const val WIDGET_TYPE_EXTRA = "widgetType"
        private const val WIDGET_IDS_EXTRA = "wids"
        private const val WIDGET_CITY_EXTRA = "city"
        private const val ACTION_EXTRA = "action"

        enum class SyncActions{ ENABLE, UPDATE}

        fun uploadData(cities: Array<String>, wIds: IntArray, cls: Class<*>, context: Context, action: SyncActions){
            val alarm = context.applicationContext?.getSystemService(Context.ALARM_SERVICE) as AlarmManager? ?: return
            val updaterIntent = Intent(context.applicationContext, WeatherSyncService::class.java)
            updaterIntent.putExtra(WIDGET_TYPE_EXTRA, cls.canonicalName)
            updaterIntent.putExtra(WIDGET_IDS_EXTRA, wIds)
            updaterIntent.putExtra(WIDGET_CITY_EXTRA, cities)
            updaterIntent.putExtra(ACTION_EXTRA, action.ordinal)

            val pi = PendingIntent.getService(
                context.applicationContext,
                0,
                updaterIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            alarm.cancel(pi)

            val nextAlarm = System.currentTimeMillis()
            when{
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarm, pi)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> alarm.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarm,
                    pi
                )
                else -> alarm.set(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarm,
                    pi
                )
            }
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        val cls = intent?.getStringExtra(WIDGET_TYPE_EXTRA)
        val wIds = intent?.getIntArrayExtra(WIDGET_IDS_EXTRA)
        val action = SyncActions.values()[intent?.getIntExtra(ACTION_EXTRA, SyncActions.UPDATE.ordinal) ?: SyncActions.UPDATE.ordinal]
        val city = intent?.getStringArrayExtra(WIDGET_CITY_EXTRA)

        if(cls == null ||  wIds == null || city == null || wIds.isEmpty() || city.isEmpty() || wIds.size != city.size){
            if(cls != null)
                WidgetController.notifyWidgets(cls, this, false)
            return
        }

        val ctx = this

        for(i in 0..wIds.size){
            if(city[i].isNotEmpty())
                request(city[i], wIds[i], cls, ctx, action)
        }
    }

    private fun request(city: String, wId: Int, cls: String, ctx: Context, action: SyncActions){
        val weatherRequester = Requester()
        val call = weatherRequester.getCurrentWeatherByCityName(city)

        call.enqueue(object: Callback<CurrentWeatherResponse> {
            override fun onResponse(call: Call<CurrentWeatherResponse>, response: Response<CurrentWeatherResponse>) {
                if (response.isSuccessful) {
                    val seriesDetailsResult = response.body()
                    seriesDetailsResult?.let {
                        WidgetData.saveWidgetData(ctx, wId, it)
                    }
                }
                WidgetController.notifyWidget(wId, cls, ctx, false)
            }

            override fun onFailure(call: Call<CurrentWeatherResponse>, t: Throwable) {
                WidgetController.notifyWidget(wId, cls, ctx, false)
            }
        })
    }
}