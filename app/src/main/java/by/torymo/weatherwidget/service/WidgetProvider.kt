package by.torymo.weatherwidget.service

import by.torymo.weatherwidget.R


class WidgetProvider: BasicWidgetProvider() {
    override fun widgetProviderClass(): Class<*> {
        return WidgetProvider::class.java
    }

    companion object{
        fun remoteView(width: Int, height: Int): Int {
            return when(getCells(width)){
                1 -> R.layout.widget1x1
                2 -> R.layout.widget2x1
                3 -> R.layout.widget3x1
                else -> R.layout.widget4x1
            }
        }
    }

    override fun widgetType(): Int {
        return R.layout.widget4x1
    }

}