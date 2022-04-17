package kr.susemi99.gpstoscreen

import android.graphics.PointF
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

class MainActivityViewModel : ViewModel() {
  private val _mapImage = mutableStateOf(R.drawable.map_1)
  val mapImage: State<Int> = _mapImage

  private val _mapViewSize = mutableStateOf(IntSize.Zero)
  val mapViewSize: State<IntSize> = _mapViewSize

  private var leftTopLocation = Location("")
  private var rightTopLocation = Location("")
  private var leftBottomLocation = Location("")

  private var theta = 0.0
  private var longitudeEquation = LinearEquation(0.0, 0.0)
  private var latitudeEquation = LinearEquation(0.0, 0.0)

  private val _pins = mutableStateListOf<IntOffset>()
  val pins: SnapshotStateList<IntOffset> = _pins

  fun updateMapViewSize(size: IntSize) {
    if (size == mapViewSize.value) {
      return
    }

    _mapViewSize.value = size
    setupMap1()
  }

  fun setupMap1() {
    _mapImage.value = R.drawable.map_1

    leftTopLocation = Location("").apply {
      latitude = 37.583744
      longitude = 126.502398
    }

    rightTopLocation = Location("").apply {
      latitude = 37.901339
      longitude = 127.765617
    }

    leftBottomLocation = Location("").apply {
      latitude = 37.027601
      longitude = 126.723336
    }

    calculateLocation()
  }

  fun setupMap2() {
    _mapImage.value = R.drawable.map_2

    leftTopLocation = Location("").apply {
      latitude = 37.345171
      longitude = 127.988737
    }

    rightTopLocation = Location("").apply {
      latitude = 37.027601
      longitude = 126.723336
    }

    leftBottomLocation = Location("").apply {
      latitude = 37.901339
      longitude = 127.765617
    }

    calculateLocation()
  }

  private fun calculateLocation() {
    val leftTopGpsPosition = GpsScreenPosition(0, 0, leftTopLocation.latitude, leftTopLocation.longitude)
    val rightTopGpsPosition = GpsScreenPosition(mapViewSize.value.width, 0, rightTopLocation.latitude, rightTopLocation.longitude)
    val leftBottomGpsPosition = GpsScreenPosition(0, mapViewSize.value.height, leftBottomLocation.latitude, leftBottomLocation.longitude)

    theta = calcTheta(
      convertUnitToLat(leftTopGpsPosition.longitude),
      leftTopGpsPosition.latitude,
      convertUnitToLat(rightTopGpsPosition.longitude),
      rightTopGpsPosition.latitude
    ) * -1

    val horizontalCoordinatesAfterRotation = calcCoordinatesAfterRotation(
      convertUnitToLat(leftTopGpsPosition.longitude), leftTopGpsPosition.latitude,
      convertUnitToLat(rightTopGpsPosition.longitude), rightTopGpsPosition.latitude, theta
    )
    horizontalCoordinatesAfterRotation.x = convertUnitToLon(horizontalCoordinatesAfterRotation.x.toDouble()).toFloat()
    rightTopGpsPosition.rotatedLongitude = horizontalCoordinatesAfterRotation.x.toDouble()
    rightTopGpsPosition.rotatedLatitude = horizontalCoordinatesAfterRotation.y.toDouble()

    val verticalCoordinatesAfterRotation = calcCoordinatesAfterRotation(
      convertUnitToLat(leftTopGpsPosition.longitude), leftTopGpsPosition.latitude,
      convertUnitToLat(leftBottomGpsPosition.longitude), leftBottomGpsPosition.latitude, theta
    )
    verticalCoordinatesAfterRotation.x = convertUnitToLon(verticalCoordinatesAfterRotation.x.toDouble()).toFloat()
    leftBottomGpsPosition.rotatedLongitude = verticalCoordinatesAfterRotation.x.toDouble()
    leftBottomGpsPosition.rotatedLatitude = verticalCoordinatesAfterRotation.y.toDouble()

    longitudeEquation = makeLinearEquation(leftTopGpsPosition.longitude, leftTopGpsPosition.x.toDouble(), rightTopGpsPosition.rotatedLongitude, rightTopGpsPosition.x.toDouble())
    latitudeEquation = makeLinearEquation(leftTopGpsPosition.latitude, leftTopGpsPosition.y.toDouble(), leftBottomGpsPosition.rotatedLatitude, leftBottomGpsPosition.y.toDouble())

    showLocationPin()
  }

  private fun showLocationPin() {
    _pins.clear()
    _pins.add(locationToIntOffset(37.386341, 127.893554))
    _pins.add(locationToIntOffset(37.515625, 127.297432))
    _pins.add(locationToIntOffset(37.827975, 127.674172))
    _pins.add(locationToIntOffset(37.162741, 126.776208))
    _pins.add(locationToIntOffset(37.555538, 126.623294))
    _pins.add(locationToIntOffset(37.237769, 127.426801))
  }

  private fun locationToIntOffset(lat: Double, lng: Double): IntOffset {
    val rotation = calcCoordinatesAfterRotation(
      convertUnitToLat(leftTopLocation.longitude), leftTopLocation.latitude,
      convertUnitToLat(lng), lat, theta
    )
    rotation.x = convertUnitToLon(rotation.x.toDouble()).toFloat()
    val x = longitudeEquation.slope * rotation.x + longitudeEquation.intercept
    val y = latitudeEquation.slope * rotation.y + latitudeEquation.intercept
    return IntOffset(x.toInt(), y.toInt())
  }

  private fun calcDistancePer(): PointF {
    val distancePerLat = leftTopLocation.distanceTo(Location("temp").apply {
      latitude = leftTopLocation.latitude + 1
      longitude = leftTopLocation.longitude
    }) / 1000 // 1경도당 거리(km)
    val distancePerLon = leftTopLocation.distanceTo(Location("temp").apply {
      latitude = leftTopLocation.latitude
      longitude = leftTopLocation.longitude + 1
    }) / 1000 // 1위도당 거리(km)

    return PointF(distancePerLat, distancePerLon)
  }

  private fun convertUnitToLat(lonValue: Double): Double {
    val distancePer = calcDistancePer()
    return lonValue * distancePer.y / distancePer.x
  }

  private fun convertUnitToLon(latValue: Double): Double {
    val distancePer = calcDistancePer()
    return latValue * distancePer.x / distancePer.y
  }

  private fun calcTheta(origin_x: Double, origin_y: Double, x: Double, y: Double): Double {
    val a = y - origin_y
    val b = x - origin_x
    return atan(a / b)
  }

  private fun calcCoordinatesAfterRotation(x1: Double, y1: Double, x2: Double, y2: Double, theta: Double): PointF {
    val rebasedX = x2 - x1
    val rebasedY = y2 - y1

    val rotatedX = rebasedX * cos(theta) - rebasedY * sin(theta)
    val rotatedY = rebasedX * sin(theta) + rebasedY * cos(theta)

    val xx = rotatedX + x1
    val yy = rotatedY + y1

    return PointF(xx.toFloat(), yy.toFloat())
  }

  private fun makeLinearEquation(x1: Double, y1: Double, x2: Double, y2: Double): LinearEquation {
    val x = x2 - x1
    val y = y2 - y1
    val slope = y / x
    val intercept = y1 - (slope * x1)
    return LinearEquation(slope, intercept)
  }
}