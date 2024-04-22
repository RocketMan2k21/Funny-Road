package com.bestdeveloper.funnyroad.model

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.BuildConfig
import com.google.maps.android.PolyUtil

class Route(
    var encodedPolyline: String,
    var distance: Double,
    var rideType: RideType,
    var routeType: RouteType
) {

    fun substringLatLng(point: LatLng): String {
        return point.latitude.toString() + "," + point.longitude
    }

    val decodedPolyline: List<LatLng>
        get() = PolyUtil.decode(encodedPolyline)

    fun setPolyline_encoded(polyline_encoded: String) {
        encodedPolyline = polyline_encoded
    }


    @BindingAdapter("imageRoutePath")
    fun loadImage(imageView: ImageView, encodedPolyline: String) {
        val imagePath = "https://maps.googleapis.com/maps/api/staticmap?path=enc:${encodedPolyline}" +
                "&key=${com.bestdeveloper.funnyroad.BuildConfig.MAPS_API_KEY}" +
                "&size=500x500"
        Glide.with(imageView.context)
            .load(imagePath)
            .into(imageView)
    }

    enum class RouteType {
        CIRCLE, STRAIGHT
    }
}