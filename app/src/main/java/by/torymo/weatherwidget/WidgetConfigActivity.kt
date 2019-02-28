package by.torymo.weatherwidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_widget_config.*
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.widget.SeekBar
import kotlinx.android.synthetic.main.widget4x1.view.*
import android.view.*


class WidgetConfigActivity : AppCompatActivity() {

    private var whiteTheme = false
    private lateinit var view: View
    private var transparency: Int = 0
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        whiteTheme = getString(R.string.widget_background_pref_default).toBoolean()
        transparency = getString(R.string.widget_transparency_pref_default).toInt()
        sbWidgetTransparency.progress = transparency

        val wallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperDrawable = wallpaperManager.drawable
        clConfigMain.setBackgroundDrawable(wallpaperDrawable)

        view = LayoutInflater.from(this).inflate(R.layout.widget4x1, null)

        val wd = WidgetData(this, true)
        view.tvDate?.text = wd.formattedDate()
        view.tvCityName?.text = wd.cityName
        view.ivWeatherIcon?.setImageDrawable(resources.getDrawable(wd.icon))
        view.tvTemperature?.text = wd.formattedTemperature()
        view.tvWind?.text = wd.formattedWind()
        view.tvPressure?.text = wd.formattedPressure()
        view.tvCloudiness?.text = wd.formattedCloudiness()


        sbWidgetTransparency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                widgetAppearanceChanged()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
            }
        })

        widgetAppearanceChanged()

        llWidgetBackground.setOnClickListener{backgroundLineClickListener()}
    }

    private fun widgetAppearanceChanged(){
        val bgColor = if(whiteTheme)resources.getColor(R.color.widget_white_bgColor) else resources.getColor(R.color.widget_dark_bgColor)
        val txtColor = if(whiteTheme)resources.getColor(R.color.widget_white_textColor) else resources.getColor(R.color.widget_dark_textColor)
        val r = Color.red(bgColor)
        val g = Color.green(bgColor)
        val b = Color.blue(bgColor)

        view.setBackgroundColor(Color.argb(sbWidgetTransparency.progress, r, g, b))
        view.tvCityName?.setTextColor(txtColor)
        view.tvTemperature?.setTextColor(txtColor)
        view.tvWind?.setTextColor(txtColor)
        view.tvPressure?.setTextColor(txtColor)
        view.tvCloudiness?.setTextColor(txtColor)
        view.tvDate?.setTextColor(txtColor)

        val width = resources.displayMetrics.widthPixels
        val height = width/4

        if (view.measuredHeight <= 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
        }
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)

        ivWidgetPreview.setImageBitmap(bitmap)
    }

    private fun backgroundLineClickListener(){
        whiteTheme = !whiteTheme
        cbWidgetBackground.isChecked = whiteTheme
        widgetAppearanceChanged()
    }

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.widget_config_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_done -> showAppWidget()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
