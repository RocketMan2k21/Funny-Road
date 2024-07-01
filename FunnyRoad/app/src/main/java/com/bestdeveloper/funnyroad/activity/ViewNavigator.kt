package com.bestdeveloper.funnyroad.activity

import android.app.Activity
import android.content.Intent


class ViewNavigator {
     fun navigateToMapActivity(activity: Activity) {
        val intent = Intent(activity, MapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }
    fun navigateToSignUpActivity(activity: Activity){
        val intent = Intent(activity, SignUpActivity::class.java)
        activity.startActivity(intent)
    }
}