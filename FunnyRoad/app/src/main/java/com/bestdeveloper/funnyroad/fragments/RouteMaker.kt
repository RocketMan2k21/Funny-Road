package com.bestdeveloper.funnyroad.fragments

import android.app.Application
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bestdeveloper.funnyroad.BuildConfig
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.api.RetrofitService
import com.bestdeveloper.funnyroad.model.RideType
import com.bestdeveloper.funnyroad.model.Route
import com.bestdeveloper.funnyroad.model.SnappedPoint
import com.bestdeveloper.funnyroad.model.SnappedPointResult
import com.bestdeveloper.funnyroad.service.SnappingPointCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import de.p72b.maps.animation.AnimatedPolyline
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors
import java.util.function.Consumer

class RouteMaker(
    private val application: Application,
    private val mapViewModel: MapViewModel,
    private val mMap: GoogleMap
) {
    private val requestQueue: RequestQueue
    private var currentLocation: Location? = null

    // User distance
    private var distanceInMeters = 0.0
    private var routeDistance = 0.0
    private var snappedPoints: MutableList<LatLng> = ArrayList()
    private val rectOption = PolylineOptions()
    private var routePoly: Polyline? = null

    init {
        rectOption.color(R.color.purple_700)
        requestQueue = Volley.newRequestQueue(application.applicationContext)
    }

    // Generates a circle and markers on it
    private fun generateRouteRecursive(circleDegreeWalked: Double, circleRadius: Double) {
        Log.i(TAG, "Generation...")
        val circleCenter = calculateNewCoordinates(
            currentLocation!!.latitude,
            currentLocation!!.longitude,
            circleRadius,
            circleDegreeWalked
        )
        val pointsToSnap = getPointsOnCircumference(
            circleRadius,
            currentLocation,
            circleCenter,
            NUM_OF_POINTS_ON_CIRC
        )
        snapPoints(java.lang.String.join("|", *toPath(pointsToSnap))) {
            routeDistance = calculateRouteDistance()
            snappedPoints.add(snappedPoints[0])
            //volleyResponse()
            moveCameraToRoute(circleCenter)
            Toast.makeText(
                application.applicationContext,
                "Route found Dist:$routeDistance",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Call this method to start the recursive route generation
    fun generateRoute(currentLocation: Location?, distanceInMeters: Double) {
        this.currentLocation = currentLocation
        this.distanceInMeters = distanceInMeters
        if (!snappedPoints.isEmpty()) {
            snappedPoints.clear()
            routeDistance = 0.0
            mMap.clear()
        }
        val executorService = Executors.newSingleThreadExecutor()
        executorService.execute {
            generateRouteRecursive(
                getRandomNumber(
                    0.0,
                    CIRCLE_DEGREE.toDouble()
                ), getCircleRadius(distanceInMeters)
            )
        }
    }

    private fun getRandomNumber(min: Double, max: Double): Double {
        return Math.random() * (max - min) + min
    }

    private fun getCircleRadius(circumference: Double): Double {
        val v = circumference / (2 * Math.PI)
        Log.v(TAG, "Calc. circle radius: $v")
        return v
    }

    private fun toPath(pointsToSnap: List<LatLng>): Array<String?> {
        val path = arrayOfNulls<String>(pointsToSnap.size)
        for (coord in pointsToSnap) {
            val index = pointsToSnap.indexOf(coord)
            path[index] = coord.latitude.toString() + ',' + coord.longitude
        }
        return path
    }

    // calcute centre of circle in given distance away from current user location
    private fun calculateNewCoordinates(
        lat: Double,
        lon: Double,
        distanceInMeters: Double,
        bearingDegrees: Double
    ): LatLng {
        val distanceKm = distanceInMeters / 1000
        val earthRadiusKm = 6371.0 // Radius of the Earth in kilometers

        // Convert latitude and longitude from degrees to radians
        val latRad = Math.toRadians(lat)
        val lonRad = Math.toRadians(lon)

        // Convert bearing from degrees to radians
        val bearingRad = Math.toRadians(bearingDegrees)

        // Calculate new latitude
        val newLatRad = Math.asin(
            Math.sin(latRad) * Math.cos(distanceKm / earthRadiusKm) +
                    Math.cos(latRad) * Math.sin(distanceKm / earthRadiusKm) * Math.cos(bearingRad)
        )

        // Calculate new longitude
        val newLonRad = lonRad + Math.atan2(
            Math.sin(bearingRad) * Math.sin(distanceKm / earthRadiusKm) * Math.cos(latRad),
            Math.cos(distanceKm / earthRadiusKm) - Math.sin(latRad) * Math.sin(newLatRad)
        )

        // Convert back to degrees
        val newLat = Math.toDegrees(newLatRad)
        val newLon = Math.toDegrees(newLonRad)
        return LatLng(newLat, newLon)
    }

    private fun getPointsOnCircumference(
        radiusInMeters: Double,
        currentLocation: Location?,
        center: LatLng,
        numOfPoints: Int
    ): List<LatLng> {
        val slice = (360 / numOfPoints).toDouble()
        val lngArrayList: MutableList<LatLng> = ArrayList()
        for (i in 0 until numOfPoints) {
            val angle = slice * i
            lngArrayList.add(
                calculateNewCoordinates(
                    center.latitude,
                    center.longitude,
                    radiusInMeters,
                    angle
                )
            )
        }
        return lngArrayList
    }

    fun snapPoints(path: String?, callback: SnappingPointCallback) {
        val snappingPointsService = RetrofitService.getSnappingPointsService()
        val call = snappingPointsService.getSnappedPoints(true, path, BuildConfig.MAPS_API_KEY)
        call.enqueue(object : Callback<SnappedPointResult?> {
            override fun onResponse(
                call: Call<SnappedPointResult?>,
                response: Response<SnappedPointResult?>
            ) {
                val result = response.body()
                if (result != null && result.snappedPoints != null) {
                    Log.i(TAG, "Response successful")
                    result.snappedPoints.forEach(Consumer { snappedPoint: SnappedPoint ->
                        snappedPoints.add(
                            LatLng(
                                snappedPoint.location.latitude,
                                snappedPoint.location.longitude
                            )
                        )
                    })
                    callback.SnappingPointCallback()
                } else {
                    Log.e(TAG, "Retrofit: failure response")
                }
            }

            override fun onFailure(call: Call<SnappedPointResult?>, t: Throwable) {}
        })
    }

    fun showRoad() {
        // Shows animated polyline using PolylineAnimator library



        val polylineOptions = PolylineOptions() // Create a new instance
        for (point in snappedPoints) {
            polylineOptions.add(point)
        }

        // Create a new polyline using the snapped points
        routePoly = mMap.addPolyline(polylineOptions)

        // Customize the polyline appearance if needed
        routePoly!!.color =
            ContextCompat.getColor(application.applicationContext, R.color.purple_700)
        routePoly!!.width = 20f // Set the polyline width in pixels
        mapViewModel.route.value = Route(PolyUtil.encode(snappedPoints), routeDistance, RideType.WALK, Route.RouteType.CIRCLE)
    }

    private fun moveCameraToRoute(zoomToCords: LatLng) {
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                zoomToCords, DEFAULT_ZOOM.toFloat()
            )
        )
    }

    fun setSnappedPoints(snappedPoints: MutableList<LatLng>) {
        this.snappedPoints = snappedPoints
    }

    fun degreesToRadians(degrees: Double): Double {
        return degrees * Math.PI / 180
    }

    fun calculateRouteDistance(): Double {
        var distance = 0.0
        if (!snappedPoints.isEmpty()) for (i in 0 until snappedPoints.size - 1) {
            val lat1 = snappedPoints[i].latitude
            val lon1 = snappedPoints[i].longitude
            val lat2 = snappedPoints[i + 1].latitude
            val lon2 = snappedPoints[i + 1].longitude
            val φ1 = degreesToRadians(lat1)
            val φ2 = degreesToRadians(lat2)
            val Δφ = degreesToRadians(lat2 - lat1)
            val Δλ = degreesToRadians(lon2 - lon1)
            val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) + Math.cos(φ1) * Math.cos(φ2) *
                    Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            distance += EarthR * c
        }
        Log.d(TAG, "route distance in meters: $distance")
        return distance
    }

    private fun getIndexOfTheNearestPoint(lat: Double, lon: Double): Int {
        var MinDistance = Double.MAX_VALUE // Initialize to a large value
        var indexOfTheNearestPoint = -1 // Initialize to -1 indicating no point found
        val φ1 = degreesToRadians(lat) // Calculate φ1 (latitude of target point)
        if (!snappedPoints.isEmpty()) {
            for (i in snappedPoints.indices) {
                val lat1 = snappedPoints[i].latitude
                val lon1 = snappedPoints[i].longitude
                val φ2 = degreesToRadians(lat1)
                val Δφ = degreesToRadians(lat1 - lat)
                val Δλ = degreesToRadians(lon1 - lon)
                val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) + Math.cos(φ1) * Math.cos(φ2) *
                        Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
                val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                val distance = EarthR * c
                if (distance < MinDistance) {
                    MinDistance = distance // Update minimum distance
                    indexOfTheNearestPoint = i // Update index of nearest point
                }
            }
        }
        return indexOfTheNearestPoint
    }

    private fun volleyResponse() {
        val wayPoints: MutableList<LatLng> = ArrayList()
        wayPoints.add(snappedPoints[0])
        wayPoints.add(snappedPoints[snappedPoints.size - 1])
        volleyRequest(wayPoints)
    }

    private fun volleyRequest(wayPoints: List<LatLng>): List<List<LatLng>>? {
        val path: MutableList<List<LatLng>> = ArrayList()
        val currentLocationLatLng = LatLng(
            currentLocation!!.latitude, currentLocation!!.longitude
        )
        val indexOfTheNearestPoint =
            getIndexOfTheNearestPoint(currentLocation!!.latitude, currentLocation!!.longitude)
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json" +
                "?destination=" + substringLatLng(snappedPoints[indexOfTheNearestPoint]) +
                "&origin=" + substringLatLng(currentLocationLatLng) +
                "&key=" + BuildConfig.MAPS_API_KEY
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, urlDirections, null,
            { response ->
                try {
                    Log.i(TAG, "Volley: successful")
                    val routes = response.getJSONArray("routes")
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    val steps = legs.getJSONObject(0).getJSONArray("steps")
                    for (i in 0 until steps.length()) {
                        val points =
                            steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                        path.add(PolyUtil.decode(points))
                        snappedPoints.addAll(0, path[i])
                    }
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                showRoad()
            }) { error -> error.printStackTrace() }
        requestQueue.add(jsonObjectRequest)
        return null
    }

    private fun substringLatLng(point: LatLng): String {
        return point.latitude.toString() + "," + point.longitude
    }

    fun setPath(decoded_path: String?) {
        snappedPoints = PolyUtil.decode(decoded_path)
    }

    val routePath: String
        get() = PolyUtil.encode(snappedPoints)

    companion object {
        private const val TAG = "RouteGenerator"
        const val EarthR = 6371e3
        private const val CIRCLE_DEGREE = 360
        private const val NUM_OF_POINTS_ON_CIRC = 90
        private const val DEFAULT_ZOOM = 16
    }
}