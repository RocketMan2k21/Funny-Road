package com.bestdeveloper.funnyroad.fragments

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bestdeveloper.funnyroad.model.Route
import com.google.firebase.firestore.FirebaseFirestore

class MapViewModel(application: Application?) : AndroidViewModel(application!!) {
    private val firebase: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var _route: MutableLiveData<Route> = MutableLiveData<Route>()

    fun saveRoute(route: Route?) {
        val mapToSave: MutableMap<String, Route> = HashMap()
        if (route != null) {
            mapToSave["path"] = route
            firebase.collection("generated_routes")
                .add(mapToSave)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        "firebase",
                        "DocumentSnapshot added with ID: " + documentReference.id
                    )
                }
                .addOnFailureListener { e -> Log.w("firebase", "Error adding document", e) }
        } else {
            Log.w("firebase", "There is no routes to save!")
        }
    }

    internal var route:MutableLiveData<Route>
        get() {return _route}
        set(value) {_route = value}
}