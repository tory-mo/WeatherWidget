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
Classes to handle JSON Responses automatically with Gson: [Responses.kt](https://github.com/tory-mo/WeatherWidget/tree/master/app/src/main/java/by/torymo/weatherwidget/service/Responses.kt)

Interface describing requests to Retrofit: [OpenWeatherService.kt](app/src/main/java/by/torymo/weatherwidget/service/OpenWeatherService.kt)

Classes to initialize Retrofit and make requests: [Requester.kt](https://github.com/tory-mo/WeatherWidget/tree/master/app/src/main/java/by/torymo/weatherwidget/service/Requester.kt)

Service to receive data: [Requester.kt](https://github.com/tory-mo/WeatherWidget/tree/master/app/src/main/java/by/torymo/weatherwidget/WeatherSyncService.kt)

Utility functions: [util.kt](https://github.com/tory-mo/WeatherWidget/tree/master/app/src/main/java/by/torymo/weatherwidget/util.kt)

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
		if (pb) {
			uploadData(context)
		}
		update(context, pb)
	}
}
```

The code snippet above calls two functions: 
 - **update()** function, which makes all the magic with widget appearance and behavior
 - **uploadData()** function, which calls Service updating weather data
 
```kotlin
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
 
 In this function we update all view vidgets and set ClickListener at one of them with PendingIntent
 
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

The <intent-filter> element must include an <action> element with the android:name attribute, which specifies that the AppWidgetProvider accepts the ACTION_APPWIDGET_UPDATE broadcast. This is the only broadcast that you must explicitly declare. The AppWidgetManager automatically sends all other App Widget broadcasts to the AppWidgetProvider as necessary. The <meta-data> element specifies the AppWidgetProviderInfo resource and requires.

Each widget in an application needs its own declaration in AndroidManifest and AppWidgetProvider class

## Step 5: Make resized widget appearance change

When widget is resized, some of its elements might be shown in an inapropriate way. To prevent it, we might provide different layut views for non-default sizes of our widget.

First, layouts must be created for each available size. In case of WeatherWidget, layout widget info xml file says, that default widget must be 4x1 cells (android:minWidth="250dp" android:minHeight="60dp") and can be resized to 1x1 cell (android:minResizeWidth="40dp"), so we need 3 more layout files for smaller sizes.

To make those resize changes applied, there are supposed to be made changes in WidgetProvider class: it must react on **onAppWidgetOptionsChanged()** callback which is called when the widget is first placed and any time the widget is resized. There are two ways to get those callbacks: using **onAppWidgetOptionsChanged()** callback or check it in **onReceive()**

 The basic logic is next: determine size of the widget and provide corresponding layout file for RemoteViews. There are two functions helping obtain the right layout in the next code snippet
```kotlin
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
	return Math.floor(((size + 30) / 70).toDouble()).toInt()
}
```

**update()** function is appended with next code lines:
```kotlin
val options = appWidgetManager.getAppWidgetOptions(widgetId)
val widgetWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
val widgetHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

val remoteViews = getRemoteViews(context, widgetWidth, widgetHeight)
```

## Step 6: Create Widget Preview

Android emulators include an application called "Widget Preview". To create a preview image, launch this application, select the app widget for your application. Then take a snapshot. To save it on your computer, run next command:
> adb pull sdcard/Download/{name_of_snapshot}.png {path_on_computer}

After that you need to place it to application's drawable resources and add it to widget layout

> android:previewImage="@drawable/preview"

## Step 7: App Widget Configuration Activity

The configuration Activity should be declared as a normal Activity in the Android manifest file. However, it will be launched by the App Widget host with the ACTION_APPWIDGET_CONFIGURE action, so the Activity needs to accept this Intent

```xml
<activity android:name=".WidgetConfigActivity">
	<intent-filter>
		<action android:name="android.appwidget.action.APPWIDGET_CONFIGURE"/>
	</intent-filter>
</activity>
```

Also, this Activity must be declared in the AppWidgetProviderInfo XML file, with the **android:configure**. Notice that the Activity is declared with a fully-qualified namespace, because it will be referenced from outside the package scope

```xml
...
android:configure="by.torymo.weatherwidget.WidgetConfigActivity"
...
```

There are two important things to remember when you implement the configuration Activity:
 - The configuration Activity should **always return a result**. The result should include the **App Widget ID** passed by the Intent that launched the Activity (saved in the Intent extras as EXTRA_APPWIDGET_ID).
 - The the system **will not send the ACTION_APPWIDGET_UPDATE** broadcast when a configuration Activity is launched. It is the responsibility of the configuration Activity to request an update from the AppWidgetManager when the App Widget is first created. However, onUpdate() will be called for subsequent updates.
 
First thing to do in configuration activity is to get id of the widget. Without it it's impossible accept settings to appropriate widget

```kotlin
val extras = intent.extras
	if (extras != null) {
		appWidgetId = extras.getInt(
			AppWidgetManager.EXTRA_APPWIDGET_ID,
			AppWidgetManager.INVALID_APPWIDGET_ID)
	}
```

When all confugurations are done, it's time to apply changes and update widget. Remember, that there must be called widget update

```kotlin
private fun showAppWidget() {
	//If the intent doesn’t have a widget ID, then call finish()//
	if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
		finish()
	}

	//Perform the configuration and get an instance of the AppWidgetManager//
	val sp = PreferenceManager.getDefaultSharedPreferences(this)
	val editor = sp.edit()

	editor.putBoolean(getString(R.string.widget_background_pref_key), whiteTheme)
	editor.putInt(getString(R.string.widget_transparency_pref_key), sbWidgetTransparency.progress)
	editor.apply()

	val resultValue = Intent()
	resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
	setResult(RESULT_OK, resultValue)
	finish()
}
```

Code of updating function in WidgetProvider must be changed in a way to use and apply changing parameters of the widget.

#### Other links

- [Build an App Widget](https://developer.android.com/guide/topics/appwidgets/)
- [App Widget Design Guidelines](https://developer.android.com/guide/practices/ui_guidelines/widget_design)
