package io.yavero.aterna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.defaultComponentContext
import io.yavero.aterna.navigation.DefaultAppRootComponent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootComponent = DefaultAppRootComponent(
            componentContext = defaultComponentContext()
        )

        setContent {
            App(rootComponent = rootComponent)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}