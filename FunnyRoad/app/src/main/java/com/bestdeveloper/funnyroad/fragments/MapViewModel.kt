package com.bestdeveloper.funnyroad.fragments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bestdeveloper.funnyroad.model.Route
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.firestore.FirebaseFirestore

class MapViewModel() : ViewModel() {
    private var TAG : String = MapViewModel::class.java.simpleName
    private var firestore: FirebaseFirestore
    private var _route: MutableLiveData<Route> = MutableLiveData<Route>()
    private var _routes: MutableLiveData<ArrayList<Route>> = MutableLiveData()
    private var _distance : MutableLiveData<Int> = MutableLiveData()
    private var _cameraPosition: MutableLiveData<CameraPosition> = MutableLiveData()
    private var initialDistance = 1500

    init {
        firestore = FirebaseFirestore.getInstance()
        listenToRoutes()
    }


    fun saveRoute(
        route: Route
    ) {
        val document =
            if (route.routeId != null && !route.routeId.isEmpty()) {
                // updating existing
                firestore.collection("routes").document(route.routeId)
            } else {
                // create new
                firestore.collection("routes").document()
            }
        route.routeId = document.id
        val set = document.set(route)
        set.addOnSuccessListener {
            Log.d("Firebase", "document saved")
        }
        set.addOnFailureListener {
            Log.d("Firebase", "Save Failed")
        }
    }

    private fun listenToRoutes(){
        firestore.collection("routes").addSnapshotListener {
            value, error ->
            if(error != null){
                Log.w(TAG, "Listen Failed", error)
                return@addSnapshotListener
            }

            if(value != null){
                val allRoutes = ArrayList<Route>()
                val documents = value.documents
                documents.forEach {
                    val route = it.toObject(Route::class.java)
                    if(route != null){
                        route.routeId = it.id
                        Log.d(TAG, "Route: ${route.toString()}")
                        allRoutes.add(route)
                    }
                }
                _routes.value = allRoutes
            }

        }
    }

    internal fun deleteRoute(route: Route) {
        if (route.routeId != null) {
            val document = firestore.collection("routes").document(route.routeId)
            val task = document.delete();
            task.addOnSuccessListener {
                Log.e(TAG, "Route ${route.routeId} Deleted")
            }
            task.addOnFailureListener {
                Log.e(TAG, "Route ${route.routeId} Failed to delete.  Message: ${it.message}")
            }
        }
    }

    internal var route:MutableLiveData<Route>
        get() {return _route}
        set(value) {_route = value}

    internal var routes:MutableLiveData<ArrayList<Route>>
        get() {return _routes}
        set(value) {_routes = value}

    internal var cameraPosition:MutableLiveData<CameraPosition>
        get() {return _cameraPosition}
        set(value) {_cameraPosition = value}

    fun getInitialDistanceCount(): MutableLiveData<Int>{
            _distance.value = initialDistance
            return _distance
    }

    fun updateCounter(value: Int) {
        val new_value = (_distance.value ?: initialDistance) + value
        if (new_value > 0)
            _distance.value = new_value
    }

    fun onButtonClick(isIncrement: Boolean) {
        val value = if (isIncrement) 200 else -200
        updateCounter(value)
    }


}



