package com.quietdiscipline.app.freeze

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import com.quietdiscipline.app.ui.freeze.FreezeScreen
import com.quietdiscipline.app.ui.theme.QuietDisciplineTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 全屏冷冻 Activity
 * 冷冻期间显示等待界面，禁用返回键，防止用户绕过
 */
@AndroidEntryPoint
class FreezeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 禁用返回键
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 冷冻期间不允许返回
            }
        })

        val packageName = intent.getStringExtra(FreezeManager.EXTRA_FROZEN_PACKAGE) ?: ""
        val freezeMinutes = intent.getIntExtra(FreezeManager.EXTRA_FREEZE_MINUTES, 5)

        setContent {
            QuietDisciplineTheme {
                FreezeScreen(
                    frozenPackage = packageName,
                    freezeMinutes = freezeMinutes
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        // 用户按Home键时立即弹回冷冻界面
        super.onUserLeaveHint()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            // 失去焦点时尝试重新获取焦点（防止切到其他应用）
            // 注意：这在部分系统上可能被限制
        }
    }
}
