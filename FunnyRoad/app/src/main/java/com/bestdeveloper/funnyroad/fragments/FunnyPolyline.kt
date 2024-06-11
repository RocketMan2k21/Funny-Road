package com.bestdeveloper.funnyroad.fragments

import android.content.Context
import android.graphics.Color
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.service.Utils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

class FunnyPolyline(
    private var context: Context,
    private var googleMap : GoogleMap
    ) {
    private val arrows = arrayListOf<Polyline>()

    /**
     * Draw a GoogleMap Polyline with an endcap arrow between the 2 locations.
     *
     * @param context - a valid context object
     * @param googleMap - a valid googleMap object
     * @param fromLatLng - the starting position
     * @param toLatLng - the ending position
     * @return Polyline - the new Polyline object
     */
    fun drawPolylineWithArrowEndcap(
        fromLatLng: LatLng,
        toLatLng: LatLng
    ): Polyline {
        val arrowColor =
            Color.WHITE // change this if you want another color (Color.BLUE)
        val lineColor = Color.parseColor("#8900f2")
        val endCapIcon =
            Utils.getEndCapIcon(context, arrowColor)

        // have googleMap create the line with the arrow endcap
        // NOTE:  the API will rotate the arrow image in the direction of the line
        val addPolyline = googleMap.addPolyline(
            PolylineOptions()
                .geodesic(true)
                .color(lineColor)
                .width(5f)
                .startCap(RoundCap())
                .endCap(CustomCap(endCapIcon))
                .jointType(JointType.ROUND)
                .add(fromLatLng, toLatLng)
        )
        arrows.add(addPolyline)
        return addPolyline
    }

    // Defines places for arrows on drawn polyline
    // Accepts list of points, which are drawn to polyline and step - the
    // interval beetween points
    // Returns Hashmap with start and end points where the arrow will be placed
    fun getArrowsPlacesOnPolyline(points: List<LatLng>, zoom: Float): HashMap<LatLng, LatLng> {
        val arrowsOnPointsList = HashMap<LatLng, LatLng>()

        if (points.size < 2) {
            return arrowsOnPointsList
        }

        val numberOfArrows = getNumberOfArrows(zoom, points)
        if (numberOfArrows == 0) {
            return arrowsOnPointsList
        }

        val step = points.size / numberOfArrows

        var i = 0
        while (i < points.size - 1) {
            val startP = points[i]
            val endP = points[i+1]
            arrowsOnPointsList[startP] = endP

            i += step
        }

        return arrowsOnPointsList
    }

    fun updateArrowsOnPolyline(zoom: Float, points:List<LatLng>) {
        // Видаліть старі стрілки (якщо є)
        removeOldArrows()

        // Отримуємо місця для нових стрілок
        val arrowsPlacesOnPolyline = getArrowsPlacesOnPolyline(points, zoom)

        // Додаємо нові стрілки на полілінію
        for (place in arrowsPlacesOnPolyline) {
            drawPolylineWithArrowEndcap(place.key , place.value)
        }
    }

    fun getNumberOfArrows(zoom: Float, points: List<LatLng>): Int {
        val koef: Int = when {
            zoom > 20 -> 6
            zoom > 18 || zoom < 20 -> 8
            zoom > 15 || zoom < 18 -> 10
            zoom > 12 || zoom < 15 -> 12
            else -> 50
        }
        return points.size / koef
    }

    private fun removeOldArrows() {
        for (arrow in arrows) {
           arrow.remove()
        }
    }


}