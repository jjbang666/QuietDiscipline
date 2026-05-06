package com.quietdiscipline.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.quietdiscipline.app.ui.navigation.NavGraph
import com.quietdiscipline.app.ui.theme.QuietDisciplineTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QuietDisciplineTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
