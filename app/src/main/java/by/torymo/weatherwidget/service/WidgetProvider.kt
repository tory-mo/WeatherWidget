package by.torymo.weatherwidget.service

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.ComponentName
import android.preference.PreferenceManager
import android.widget.RemoteViews
import by.torymo.weatherwidget.*
import android.app.PendingIntent
import android.view.View
import android.app.AlarmManager
import android.os.Build
import android.content.Context.ALARM_SERVICE
import android.graphics.Color


class WidgetProvider: AppWidgetProvider() {

    companion object {
        const val PROGRESS_BAR_EXTRA = "pbex"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent?.action || AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED == intent?.action) {
            val pb = intent.getBooleanExtra(PROGRESS_BAR_EXTRA, false)
            if (pb) uploadData(context)
            update(context, pb)
        }else if(AppWidgetManager.ACTION_APPWIDGET_ENABLED == intent?.action){
            uploadData(context)
        }
    }

    private fun uploadData(context: Context?){
        val alarm = context?.applicationContext?.getSystemService(ALARM_SERVICE) as AlarmManager? ?: return
        val updaterIntent = Intent(context?.applicationContext, WeatherSyncService::class.java)

        val pi = PendingIntent.getService(
            context?.applicationContext,
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

    private fun update(context: Context?, pb: Boolean){
        val appWidgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetProvider::class.java)) ?: return

        val appWidgetManager = AppWidgetManager.getInstance(context)

        val wd = WidgetData(context)
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val whiteTheme = sp.getBoolean(context?.getString(R.string.widget_background_pref_key), context?.getString(R.string.widget_background_pref_default)!!.toBoolean())
        val transparency = sp.getInt(context.getString(R.string.widget_transparency_pref_key), context.getString(R.string.widget_transparency_pref_default)!!.toInt())

        for (widgetId in appWidgetIds) {
            val options = appWidgetManager.getAppWidgetOptions(widgetId)
            val widgetWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val widgetHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            val remoteViews = getRemoteViews(context, widgetWidth, widgetHeight)

            remoteViews.setTextViewText(R.id.tvDate, wd.formattedDate())
            remoteViews.setTextViewText(R.id.tvCityName, wd.cityName)
            remoteViews.setTextViewText(R.id.tvTemperature, wd.formattedTemperature())
            remoteViews.setTextViewText(R.id.tvPressure, wd.formattedPressure())
            remoteViews.setTextViewText(R.id.tvWind, wd.formattedWind())
            remoteViews.setTextViewText(R.id.tvCloudiness, wd.formattedCloudiness())
            if(wd.icon != -1) remoteViews.setImageViewResource(R.id.ivWeatherIcon, wd.icon)

            if (pb) {
                remoteViews.setViewVisibility(R.id.pbWidgetRefresh, View.VISIBLE)
                remoteViews.setViewVisibility(R.id.ivRefresh, View.INVISIBLE)
            } else {
                remoteViews.setViewVisibility(R.id.ivRefresh, View.VISIBLE)
                remoteViews.setViewVisibility(R.id.pbWidgetRefresh, View.INVISIBLE)

                val refreshIntent = Intent(context, WidgetProvider::class.java)
                refreshIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                refreshIntent.putExtra(PROGRESS_BAR_EXTRA, true)
                val refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0)
                remoteViews.setOnClickPendingIntent(R.id.ivRefresh, refreshPendingIntent)
                remoteViews.setOnClickPendingIntent(R.id.tvCityName, refreshPendingIntent)
            }

            var bgColor = context.resources.getColor(R.color.widget_dark_bgColor)
            if(whiteTheme){
                remoteViews.setTextColor(R.id.tvDate, context.resources.getColor(R.color.widget_white_textColor))
                remoteViews.setTextColor(R.id.tvCityName, context.resources.getColor(R.color.widget_white_textColor))
                remoteViews.setTextColor(R.id.tvTemperature, context.resources.getColor(R.color.widget_white_textColor))
                remoteViews.setTextColor(R.id.tvPressure, context.resources.getColor(R.color.widget_white_textColor))
                remoteViews.setTextColor(R.id.tvWind, context.resources.getColor(R.color.widget_white_textColor))
                remoteViews.setTextColor(R.id.tvCloudiness, context.resources.getColor(R.color.widget_white_textColor))
                remoteViews.setInt(R.id.rlMain, "setBackgroundColor", context.resources.getColor(R.color.widget_white_bgColor))

                bgColor = context.resources.getColor(R.color.widget_white_bgColor)

            }else{
                remoteViews.setTextColor(R.id.tvDate, context.resources.getColor(R.color.widget_dark_textColor))
                remoteViews.setTextColor(R.id.tvCityName, context.resources.getColor(R.color.widget_dark_textColor))
                remoteViews.setTextColor(R.id.tvTemperature, context.resources.getColor(R.color.widget_dark_textColor))
                remoteViews.setTextColor(R.id.tvPressure, context.resources.getColor(R.color.widget_dark_textColor))
                remoteViews.setTextColor(R.id.tvWind, context.resources.getColor(R.color.widget_dark_textColor))
                remoteViews.setTextColor(R.id.tvCloudiness, context.resources.getColor(R.color.widget_dark_textColor))
                remoteViews.setInt(R.id.rlMain, "setBackgroundColor", context.resources.getColor(R.color.widget_dark_bgColor))
            }

            val r = Color.red(bgColor)
            val g = Color.green(bgColor)
            val b = Color.blue(bgColor)

            remoteViews.setInt(R.id.rlMain, "setBackgroundColor", Color.argb(transparency, r, g, b))

            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }
    }

    private fun getRemoteViews(context: Context?, width: Int, height: Int): RemoteViews{
        val cells = getCells(width)
        return when(cells){
            1 -> RemoteViews(context?.packageName, R.layout.widget1x1)
            2 -> RemoteViews(context?.packageName, R.layout.widget2x1)
            3 -> RemoteViews(context?.packageName, R.layout.widget3x1)
            else -> RemoteViews(context?.packageName, R.layout.widget4x1)
        }
    }

    //those numbers from developer.android.com
    private fun getCells(size: Int): Int {
        return Math.ceil(((size + 30) / 70).toDouble()).toInt()
    }
}