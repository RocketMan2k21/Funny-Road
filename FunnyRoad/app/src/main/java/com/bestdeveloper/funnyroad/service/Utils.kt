package com.bestdeveloper.funnyroad.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import com.bestdeveloper.funnyroad.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object Utils {
    const val SNAPPING_BASE_URL = "https://roads.googleapis.com/v1/"

    /**
     * Return a BitmapDescriptor of an arrow endcap icon for the passed color.
     *
     * @param context - a valid context object
     * @param color - the color to make the arrow icon
     * @return BitmapDescriptor - the new endcap icon
     */
    fun getEndCapIcon(context: Context?, color: Int): BitmapDescriptor {

        // mipmap icon - white arrow, pointing up, with point at center of image
        // you will want to create:  mdpi=24x24, hdpi=36x36, xhdpi=48x48, xxhdpi=72x72, xxxhdpi=96x96
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.endcap)

        // set the bounds to the whole image (may not be necessary ...)
        drawable!!.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

        // overlay (multiply) your color over the white icon
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)

        // create a bitmap from the drawable
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )

        // render the bitmap on a blank canvas
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)

        // create a BitmapDescriptor from the new bitmap
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}