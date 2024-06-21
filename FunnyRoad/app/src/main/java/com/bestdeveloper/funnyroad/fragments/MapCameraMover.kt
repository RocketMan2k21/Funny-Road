package com.bestdeveloper.funnyroad.fragments

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object MapCameraMover {
    const val DEFAULT_ZOOM = 15f
    const val TAG = "CameraMap"

    fun moveCameraToRoute(points: List<LatLng>, mMap: GoogleMap) {
        val bounds = getBound(points)
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50)

        mMap.animateCamera(cameraUpdate, 500, object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                Log.i(TAG, "Camera mooved successfully")
            }

            override fun onCancel() {

            }

        })
    }

    private fun getBound(points: List<LatLng>): LatLngBounds {
        val builder = LatLngBounds.Builder()
        for (point in points) {
            builder.include(point)
        }
        return builder.build()
    }

    fun moveCameraTo(latlng: LatLng, mMap: GoogleMap){
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM)
        mMap.animateCamera(cameraUpdate)
    }

}
