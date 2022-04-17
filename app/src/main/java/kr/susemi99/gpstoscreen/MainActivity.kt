package kr.susemi99.gpstoscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.susemi99.gpstoscreen.ui.theme.GpsToScreenTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      GpsToScreenTheme {
        val viewModel = viewModel<MainActivityViewModel>()

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
          Column(verticalArrangement = Arrangement.Center) {
            Row() {
              TextButton(onClick = { viewModel.setupMap1() }) {
                Text(text = "map1")
              }
              TextButton(onClick = { viewModel.setupMap2() }) {
                Text(text = "map2")
              }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
              Image(
                painter = painterResource(id = viewModel.mapImage.value),
                contentDescription = "",
                modifier = Modifier
                  .fillMaxWidth()
                  .onSizeChanged { viewModel.updateMapViewSize(it) },
                contentScale = ContentScale.FillBounds
              )

              viewModel.pins.forEach { PinIcon(it) }
            }
          }
        }
      }
    }
  }

  @Composable
  private fun PinIcon(offset: IntOffset) {
    var iconSizeOffset by remember { mutableStateOf(IntOffset.Zero) }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .offset { iconSizeOffset }
        .onSizeChanged {
          iconSizeOffset = IntOffset(offset.x - it.width / 2, offset.y - it.height)
        },
    ) {
      Icon(Icons.Default.LocationOn, contentDescription = "", tint = Color.Yellow)
    }
  }
}