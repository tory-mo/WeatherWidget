package by.torymo.weatherwidget



import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import by.torymo.weatherwidget.service.WidgetController
import kotlinx.android.synthetic.main.fragment_configuration.*
import kotlinx.android.synthetic.main.widget3x2.view.*
import kotlinx.android.synthetic.main.widget4x1.view.ivWeatherIcon
import kotlinx.android.synthetic.main.widget4x1.view.tvCityName
import kotlinx.android.synthetic.main.widget4x1.view.tvCloudiness
import kotlinx.android.synthetic.main.widget4x1.view.tvDate
import kotlinx.android.synthetic.main.widget4x1.view.tvPressure
import kotlinx.android.synthetic.main.widget4x1.view.tvTemperature
import kotlinx.android.synthetic.main.widget4x1.view.tvWind

class ConfigurationFragment : Fragment() {


    private var whiteTheme = false
    private var transparency: Int = 0

    private lateinit var previewView: View

    private lateinit var basicConfigActivity: BasicConfigActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)

        basicConfigActivity = (activity as BasicConfigActivity)
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


        val paramLayoutInflater = try {
            val wallpaperManager = WallpaperManager.getInstance(activity)
            if (wallpaperManager.wallpaperInfo != null) {
                wallpaperManager.wallpaperInfo.loadThumbnail(activity?.packageManager)
            } else {
                wallpaperManager.drawable
            }
        } catch (piException: Exception) {
            resources.getDrawable(R.drawable.preview_bg3)
        }

        ivWidgetPreviewBg.background = paramLayoutInflater

        previewView = LayoutInflater.from(activity).inflate(basicConfigActivity.widgetType(), null)
        widgetDataChanged()

        sbWidgetTransparency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                transparency = sbWidgetTransparency.progress
                widgetAppearanceChanged(ivWidgetPreview)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
            }
        })

        widgetAppearanceChanged(ivWidgetPreview)

        llWidgetBackground.setOnClickListener{backgroundLineClickListener(ivWidgetPreview)}


        return mainView
    }

    private fun widgetDataChanged(){
        val wd = WidgetData(activity, basicConfigActivity.appWidgetId, true)
        previewView.tvDate?.text = wd.formattedDate()
        previewView.tvCityName?.text = wd.cityName
        previewView.ivWeatherIcon?.setImageDrawable(resources.getDrawable(wd.icon))
        previewView.tvTemperature?.text = wd.formattedTemperature()
        previewView.tvWind?.text = wd.formattedWind()
        previewView.tvPressure?.text = wd.formattedPressure()
        previewView.tvCloudiness?.text = wd.formattedCloudiness()
        previewView.tvHumidity?.text = wd.formattedHumidity()
        previewView.tvWeatherDescription?.text = wd.description
        previewView.tvSunrise?.text = wd.formattedSunrise()
        previewView.tvSunset?.text = wd.formattedSunset()
    }

    private fun widgetAppearanceChanged(ivWidgetPreview: ImageView){
        val bgColor = if(whiteTheme)resources.getColor(R.color.widget_white_bgColor) else resources.getColor(R.color.widget_dark_bgColor)
        val txtColor = if(whiteTheme)resources.getColor(R.color.widget_white_textColor) else resources.getColor(R.color.widget_dark_textColor)
        val r = Color.red(bgColor)
        val g = Color.green(bgColor)
        val b = Color.blue(bgColor)

        previewView.setBackgroundColor(Color.argb(transparency, r, g, b))
        previewView.tvCityName?.setTextColor(txtColor)
        previewView.tvTemperature?.setTextColor(txtColor)
        previewView.tvWind?.setTextColor(txtColor)
        previewView.tvPressure?.setTextColor(txtColor)
        previewView.tvCloudiness?.setTextColor(txtColor)
        previewView.tvDate?.setTextColor(txtColor)
        previewView.tvHumidity?.setTextColor(txtColor)
        previewView.tvWeatherDescription?.setTextColor(txtColor)
        previewView.tvSunrise?.setTextColor(txtColor)
        previewView.tvSunset?.setTextColor(txtColor)

        val width = basicConfigActivity.widgetPreviewWidth(resources.displayMetrics.widthPixels)
        val height = basicConfigActivity.widgetPreviewHeight(resources.displayMetrics.widthPixels)

        if (previewView.measuredHeight <= 0) {
            previewView.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
        }
        val bitmap = Bitmap.createBitmap(previewView.measuredWidth, previewView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        previewView.layout(0, 0, previewView.measuredWidth, previewView.measuredHeight)
        previewView.draw(canvas)

        ivWidgetPreview.setImageBitmap(bitmap)
    }

    private fun backgroundLineClickListener(ivWidgetPreview: ImageView){
        whiteTheme = !whiteTheme
        cbWidgetBackground.isChecked = whiteTheme
        widgetAppearanceChanged(ivWidgetPreview)
    }

    private fun showAppWidget() {
        //If the intent doesn’t have a widget ID, then call finish()//
        val appWidgetId = basicConfigActivity.appWidgetId

        //Perform the configuration and get an instance of the AppWidgetManager//
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = sp.edit()
        editor.putBoolean(getString(R.string.widget_background_pref_key) + appWidgetId, whiteTheme)
        editor.putInt(getString(R.string.widget_transparency_pref_key) + appWidgetId, transparency)
        editor.apply()

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        activity?.setResult(AppCompatActivity.RESULT_OK, resultValue)
        activity?.finish()

        WidgetController.notifyWidget(appWidgetId, basicConfigActivity.widgetType(), basicConfigActivity, true)
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
