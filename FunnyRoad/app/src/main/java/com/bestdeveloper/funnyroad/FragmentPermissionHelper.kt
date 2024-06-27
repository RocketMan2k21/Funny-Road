package com.bestdeveloper.funnyroad

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

class FragmentPermissionHelper {
    fun startPermissionRequest (
        fr: FragmentActivity,
        fs : FragmentPermissionInterface,
        manifest: String
    ){
        val requestPermissionLauncher =
                fr.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    fs.onGranted(it)
                }
        requestPermissionLauncher.launch(manifest)
    }
}