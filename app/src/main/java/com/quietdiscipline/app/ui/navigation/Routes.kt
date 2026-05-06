package com.quietdiscipline.app.ui.navigation

/**
 * 导航路由常量
 */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val STATS = "stats"
    const val GUIDE = "guide"
    const val FREEZE = "freeze/{packageName}/{freezeMinutes}"

    fun freezeRoute(packageName: String, freezeMinutes: Int): String {
        return "freeze/$packageName/$freezeMinutes"
    }
}
