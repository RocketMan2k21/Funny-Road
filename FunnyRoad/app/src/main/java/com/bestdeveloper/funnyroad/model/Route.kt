package com.bestdeveloper.funnyroad.model

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bestdeveloper.funnyroad.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

data class Route (
    var encodedPolyline: String = "",
    var distance: Double = 0.0,
    var rideType: RideType = RideType.WALK,
    var routeType: RouteType = RouteType.CIRCLE,
    var routeId: String = ""
) {

    companion object {
        @JvmStatic
        @BindingAdapter(value = ["imageRoutePath", "setProgressBar"])
        fun loadImage(imageView: ImageView, path: String, progressBar: ProgressBar) {
            val imagePath = "https://maps.googleapis.com/maps/api/staticmap?path=" +
                    "color:0x000000|weight:5"+
                    "|enc:${path}" +
                    "&key=${com.bestdeveloper.funnyroad.BuildConfig.MAPS_API_KEY}" +
                    "&size=150x150&scale=2"
            Log.d("Route", "image_path: $imagePath")

            Glide.with(imageView.context)
                .load(imagePath)
                .listener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(imageView)



        }

        @JvmStatic
        @BindingAdapter("distanceText")
        fun setDistanceText(view: TextView, distance: Double) {
            view.text = "${distance.toInt()} m"
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




