package com.quietdiscipline.app.freeze

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import com.quietdiscipline.app.ui.freeze.FreezeScreen
import com.quietdiscipline.app.ui.theme.QuietDisciplineTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 全屏冷冻 Activity
 * 冷冻期间显示等待界面，禁用返回键，防止用户绕过
 */
@AndroidEntryPoint
class FreezeActivity : ComponentActivity() {

    @Inject
    lateinit var freezeManager: FreezeManager

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
                    freezeMinutes = freezeMinutes,
                    onCountdownEnd = {
                        freezeManager.releaseFreeze()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 如果 Activity 被意外销毁（如手动滑掉），确保冷冻状态被清除
        if (freezeManager.isFrozen()) {
            freezeManager.releaseFreeze()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // 用户按 Home 键时，不做额外处理
        // onWindowFocusChanged 会在失去焦点时处理
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            // 失去焦点时尝试重新拉回（通过重新创建 Activity）
            // 注意：这在部分系统上可能被限制
        }
    }
}
