# Preparations

For this project we need an API key for [openweathermap.org](https://openweathermap.org)

### Retrofit
```gradle
def retrofitVersion = '2.4.0'
implementation "com.squareup.retrofit2:retrofit:$retrofitVersion"
implementation "com.squareup.retrofit2:converter-gson:$retrofitVersion"
implementation "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2"
```
### okhttp3
```gradle
def okhttpVersion = '3.9.1'
implementation "com.squareup.okhttp3:okhttp:$okhttpVersion"
implementation "com.squareup.okhttp3:logging-interceptor:$okhttpVersion"
```
Classes to handle JSON Responses automatically with Gson: [Responses.kt](app/src/main/java/by/torymo/weatherwidget/service/Responses.kt)

Interface describing requests to Retrofit: [OpenWeatherService.kt](app/src/main/java/by/torymo/weatherwidget/service/OpenWeatherService.kt)

Classes to initialize Retrofit and make requests: [Requester.kt](app/src/main/java/by/torymo/weatherwidget/service/Requester.kt)

Service to receive data: [Requester.kt](app/src/main/java/by/torymo/weatherwidget/WeatherSyncService.kt)

Utility functions: [util.kt](app/src/main/java/by/torymo/weatherwidget/util.kt)

# Widget

## Step 1: Create layout

App Widget layouts are based on RemoteViews, and support limited list of layout kinds and view widget. A RemoteViews object (and, consequently, an App Widget) can support the following layout classes: **FrameLayout, LinearLayout, RelativeLayout, GridLayout**. And the following widget classes: **AnalogClock, Button, Chronometer, ImageButton, ImageView, ProgressBar, TextView, TextClock, ViewFlipper, ListView, GridView, StackView, AdapterViewFlipper**. Descendants of these classes are not supported. RemoteViews also supports ViewStub, which is an invisible, zero-sized View you can use to lazily inflate layout resources at runtime.

## Step 2: Add AppWidgetProviderInfo Metadata

Notice, that if several widgets are needed, for each there must be separeate widget provider file. Place it in xml resource folder.
```xml
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minResizeWidth="40dp"
    android:minHeight="60dp"
    android:initialLayout="@layout/widget4x1"
    android:resizeMode="horizontal"
    android:widgetCategory="home_screen"/>
```
Each widget must define a *minWidth* and *minHeight*, indicating the minimum amount of space it should consume by default. When users add a widget to their Home screen, it will generally occupy more than the minimum width and height you specify. When your widget is added, it will be stretched to occupy the minimum number of cells, horizontally and vertically, required to satisfy its *minWidth* and *minHeight* constraints. 

While the width and height of a cell—as well as the amount of automatic margins applied to widgets—may vary across devices, you can use the table below to roughly estimate your widget's minimum dimensions, given the desired number of occupied grid cells:

№ of Cells (Columns or Rows) | Available Size (dp) (minWidth or minHeight)
-----------------------------| -------------------------------------------
1 | 40dp
2 | 110dp
3 | 180dp
4 | 250dp
... | ...
n | 70 × n − 30

## Step 3: WidgetProvider class

The AppWidgetProvider class extends BroadcastReceiver, but receives only the event broadcasts that are relevant to the App Widget, such as when the App Widget is updated, deleted, enabled, and disabled. When these broadcast events occur, the AppWidgetProvider receives the corresponding method calls. Thus onReceive() method is called for every broadcast and before each of the callback methods. AppWidgetProvider class implementation must be declared as a broadcast receiver using the <receiver> element in the AndroidManifest (Step 4)
    
**onUpdate()** is the most important callback method, because it's called to update the App Widget at intervals defined by the *updatePeriodMillis* attribute in the AppWidgetProviderInfo. This method is also called when the user adds the App Widget, so it should perform the essential setup, such as define event handlers for Views and start a temporary Service, if necessary. However, if you have declared a configuration Activity, this method is not called when the user adds the App Widget, but is called for the subsequent updates. It is the responsibility of the configuration Activity to perform the first update when configuration is done

Instead of using **onUpdate()** callback we might use **onReceive()** just as in any BroadcastReceiver
```kotlin
override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent?.action) {
            val pb = intent.getBooleanExtra(PROGRESS_BAR_EXTRA, false)
            update(context, pb)
        }
    }
```

In this code snippet our **update()** function makes all magic
```kotlin
private fun update(context: Context?, pb: Boolean){
        val appWidgetIds = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetProvider::class.java)) ?: return

        val appWidgetManager = AppWidgetManager.getInstance(context)

        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val cityName = sp.getString(context?.getString(R.string.pref_city_name_key), "")
        val icon = getIconResourceForWeatherCondition(sp.getInt(context?.getString(R.string.pref_weather_icon_key), -1))
        val temperature = sp.getFloat(context?.getString(R.string.pref_temperature_key), 0.0f)
        val pressure = sp.getFloat(context?.getString(R.string.pref_pressure_key), 0.0f)
        val windSpeed =  sp.getFloat(context?.getString(R.string.pref_wind_speed_key), 0.0f)
        val windDirection = sp.getFloat(context?.getString(R.string.pref_wind_direction_key), 0.0f)
        val clouds = sp.getFloat(context?.getString(R.string.pref_clouds_key), 0.0f)
        val date = sp.getLong(context?.getString(R.string.pref_date_key), -1)

        for (widgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context?.packageName, R.layout.widget4x1)

            remoteViews.setTextViewText(R.id.tvDate, formattedDate(date))
            remoteViews.setTextViewText(R.id.tvCityName, cityName)
            remoteViews.setTextViewText(R.id.tvTemperature, formattedTemperature(context, temperature))
            remoteViews.setTextViewText(R.id.tvPressure, formattedPressure(context, pressure))
            remoteViews.setTextViewText(R.id.tvWind, formattedWind(context, windSpeed, windDirection))
            remoteViews.setTextViewText(R.id.tvCloudiness, formattedCloudiness(context, clouds))
            if(icon != -1) remoteViews.setImageViewResource(R.id.ivWeatherIcon, icon)

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
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }
    }
 ```
 
 In this function we update all view vidgets and set ClickListener at one of them through PendingIntent
 
## Step 4: Declaring Widget in AndroidManifest

```xml
<receiver android:name=".service.WidgetProvider">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE"/>
            </intent-filter>
            <meta-data
                    android:name="android.appwidget.provider"
                    android:resource="@xml/widget_info"/>
        </receiver>
```

The **<intent-filter>** element must include an <action> element with the *android:name* attribute, which specifies that the AppWidgetProvider accepts the ACTION_APPWIDGET_UPDATE broadcast. This is the only broadcast that you must explicitly declare. The AppWidgetManager automatically sends all other App Widget broadcasts to the AppWidgetProvider as necessary. The **<meta-data>** element specifies the AppWidgetProviderInfo resource and requires.

Each widget in an application needs its own declaration in AndroidManifest

## Step 5: Create Widget Preview

Android emulators include an application called "Widget Preview". To create a preview image, launch this application, select the app widget for your application. Then take a snapshot. To save it on your computer, run next command:
> adb pull sdcard/Download/{name_of_snapshot}.png {path_on_computer}

After that you need to place it to application's drawable resources and add it to widget layout

> android:previewImage="@drawable/preview"

#### Other links

- [Build an App Widget](https://developer.android.com/guide/topics/appwidgets/)
- [App Widget Design Guidelines](https://developer.android.com/guide/practices/ui_guidelines/widget_design)
