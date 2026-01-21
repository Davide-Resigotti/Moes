package com.moes.utils

import com.moes.data.Coordinate
import kotlin.math.roundToInt

object PolylineUtils {
    fun encode(coords: List<Coordinate>): String {
        val result = StringBuffer()
        var lastLat = 0
        var lastLng = 0

        for (point in coords) {
            val lat = (point.latitude * 1e5).roundToInt()
            val lng = (point.longitude * 1e5).roundToInt()

            val dLat = lat - lastLat
            val dLng = lng - lastLng

            encodeValue(dLat, result)
            encodeValue(dLng, result)

            lastLat = lat
            lastLng = lng
        }
        return result.toString()
    }

    fun decode(encoded: String): List<Coordinate> {
        val poly = ArrayList<Coordinate>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dLat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dLng

            val p = Coordinate(lat.toDouble() / 1e5, lng.toDouble() / 1e5)
            poly.add(p)
        }
        return poly
    }

    private fun encodeValue(value: Int, result: StringBuffer) {
        var v = value shl 1
        if (value < 0) v = v.inv()
        while (v >= 0x20) {
            result.append(((0x20 or (v and 0x1f)) + 63).toChar())
            v = v ushr 5
        }
        result.append((v + 63).toChar())
    }
}