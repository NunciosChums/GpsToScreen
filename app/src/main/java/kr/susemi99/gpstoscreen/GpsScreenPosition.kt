package kr.susemi99.gpstoscreen

/**
 * 지도의 각 꼭지점에 해당하는 GPS 좌표와 화면 좌표 묶음
 */
data class GpsScreenPosition(
  val x: Int,
  val y: Int,
  val latitude: Double,
  val longitude: Double,
  var rotatedLongitude: Double = 0.0,
  var rotatedLatitude: Double = 0.0
)
