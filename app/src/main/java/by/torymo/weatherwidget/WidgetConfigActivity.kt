package by.torymo.weatherwidget

import android.os.Bundle


class WidgetConfigActivity : BasicConfigActivity() {
    override fun widgetPreviewWidth(displayWidth: Int): Int {
        return displayWidth
    }

    override fun widgetPreviewHeight(displayWidth: Int): Int {
        return displayWidth / 4
    }

    override fun widgetType(): Int {
        return R.layout.widget4x1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
