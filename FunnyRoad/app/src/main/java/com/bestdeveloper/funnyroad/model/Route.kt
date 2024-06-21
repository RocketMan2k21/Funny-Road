package com.bestdeveloper.funnyroad.model

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

data class Route (
    var encodedPolyline: String = "",
    var distance: Double = 0.0,
    var rideType: RideType = RideType.WALK,
    var routeType: RouteType = RouteType.CIRCLE,
    var routeId: String = ""
) {

    companion object {
        @JvmStatic
        @BindingAdapter("imageRoutePath")
        fun loadImage(imageView: ImageView, path: String) {
            val imagePath = "https://maps.googleapis.com/maps/api/staticmap?path=" +
                    "color:0x8900f2|weight=20"+
                    "|enc:${path}" +
                    "&key=${com.bestdeveloper.funnyroad.BuildConfig.MAPS_API_KEY}" +
                    "&size=500x500"

            Log.d("Route", "image_path: $imagePath")
            Glide.with(imageView.context)
                .load(imagePath)
                .into(imageView)
        }

        @JvmStatic
        @BindingAdapter("distanceText")
        fun setDistanceText(view: TextView, distance: Double) {
            val decimalPart = (distance % 1).toString().substring(2) // Extract the decimal part and remove the leading "0."
            view.text = "$decimalPart m"
        }
    }

    enum class RouteType {
        CIRCLE,
        STRAIGHT
    }

    override fun toString(): String {
        return "Route(encodedPolyline='$encodedPolyline', distance=$distance, rideType=$rideType, routeType=$routeType, routeId='$routeId')"
    }




}




