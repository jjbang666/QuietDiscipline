package com.quietdiscipline.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuietDisciplineApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Hilt 初始化由 @HiltAndroidApp 自动处理
    }
}
