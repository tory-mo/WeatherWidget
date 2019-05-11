package by.torymo.weatherwidget


import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_configuration.*
import kotlinx.android.synthetic.main.widget4x1.view.*

class ConfigurationFragment : Fragment() {


    private var whiteTheme = false
    private var transparency: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mainView = inflater.inflate(R.layout.fragment_configuration, container, false)
        whiteTheme = getString(R.string.widget_background_pref_default).toBoolean()
        transparency = getString(R.string.widget_transparency_pref_default).toInt()

        val sbWidgetTransparency = mainView.findViewById<SeekBar>(R.id.sbWidgetTransparency)
        val ivWidgetPreviewBg = mainView.findViewById<ImageView>(R.id.ivWidgetPreviewBg)
        val llWidgetBackground = mainView.findViewById<LinearLayout>(R.id.llWidgetBackground)
        val ivWidgetPreview = mainView.findViewById<ImageView>(R.id.ivWidgetPreview)
        sbWidgetTransparency.progress = transparency

        var paramLayoutInflater: Drawable = resources.getDrawable(R.drawable.empty_bg)

        val wallpaperManager = WallpaperManager.getInstance(activity)
        try {
            paramLayoutInflater = if (wallpaperManager.wallpaperInfo != null) {
                wallpaperManager.wallpaperInfo.loadThumbnail(activity?.packageManager)
            } else {
                wallpaperManager.drawable
            }
        } catch (paramLayoutInflater: Exception) {
        }

        ivWidgetPreviewBg.background = paramLayoutInflater

        val view = LayoutInflater.from(activity).inflate(R.layout.widget4x1, null)

        val wd = WidgetData(activity, true)
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
                transparency = sbWidgetTransparency.progress
                widgetAppearanceChanged(view, ivWidgetPreview)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
            }
        })

        widgetAppearanceChanged(view, ivWidgetPreview)

        llWidgetBackground.setOnClickListener{backgroundLineClickListener(view, ivWidgetPreview)}


        return mainView
    }

    private fun widgetAppearanceChanged(view: View, ivWidgetPreview: ImageView){
        val bgColor = if(whiteTheme)resources.getColor(R.color.widget_white_bgColor) else resources.getColor(R.color.widget_dark_bgColor)
        val txtColor = if(whiteTheme)resources.getColor(R.color.widget_white_textColor) else resources.getColor(R.color.widget_dark_textColor)
        val r = Color.red(bgColor)
        val g = Color.green(bgColor)
        val b = Color.blue(bgColor)

        view.setBackgroundColor(Color.argb(transparency, r, g, b))
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

    private fun backgroundLineClickListener(view: View, ivWidgetPreview: ImageView){
        whiteTheme = !whiteTheme
        cbWidgetBackground.isChecked = whiteTheme
        widgetAppearanceChanged(view, ivWidgetPreview)
    }

    private fun showAppWidget() {
        //If the intent doesn’t have a widget ID, then call finish()//
        val appWidgetId = (activity as WidgetConfigActivity).appWidgetId

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Toast.makeText(activity, "Can't add a widget", Toast.LENGTH_LONG).show()
            activity?.finish()
        }

        //Perform the configuration and get an instance of the AppWidgetManager//
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = sp.edit()

        editor.putBoolean(getString(R.string.widget_background_pref_key), whiteTheme)
        editor.putInt(getString(R.string.widget_transparency_pref_key), transparency)
        editor.apply()

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        activity?.setResult(AppCompatActivity.RESULT_OK, resultValue)
        activity?.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.widget_config_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_done -> showAppWidget()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
