package com.bestdeveloper.funnyroad.fragments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bestdeveloper.funnyroad.db.UserRepository
import com.bestdeveloper.funnyroad.model.Route
import com.google.firebase.firestore.FirebaseFirestore

class MapViewModel : ViewModel() {
    private var tag : String = MapViewModel::class.java.simpleName
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var _route: MutableLiveData<Route> = MutableLiveData<Route>()
    private var _routes: MutableLiveData<ArrayList<Route>> = MutableLiveData()
    private var _distance : MutableLiveData<Int> = MutableLiveData()
    private var initialDistance = 1500
    private var userRepository = UserRepository.getInstance()

    init {
        listenToRoutes()
    }


    fun saveRoute(
        route: Route
    ) {
        val userDoc =
            firestore.collection("users").document(userRepository.getCurrentUser()!!.uid)
        val document =
            if (route.routeId.isNotEmpty()) {
                // updating existing
                    userDoc.collection("routes").document(route.routeId)
            } else {
                // create new
                userDoc.collection("routes").document()
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
        firestore.collection("users").document(userRepository.getCurrentUser()!!.uid)
            .collection("routes").addSnapshotListener {
            value, error ->
            if(error != null){
                Log.w(tag, "Listen Failed", error)
                return@addSnapshotListener
            }

            if(value != null){
                val allRoutes = ArrayList<Route>()
                val documents = value.documents
                documents.forEach {
                    val route = it.toObject(Route::class.java)
                    if(route != null){
                        route.routeId = it.id
                        Log.d(tag, "Route: $route")
                        allRoutes.add(route)
                    }
                }
                _routes.value = allRoutes
            }

        }
    }

    internal fun deleteRoute(route: Route) {
        val userDoc =
            firestore.collection("users").document(userRepository.getCurrentUser()!!.uid)
        val document = userDoc.collection("routes").document(route.routeId)
        val task = document.delete()
        task.addOnSuccessListener {
            Log.e(tag, "Route ${route.routeId} Deleted")
        }
        task.addOnFailureListener {
            Log.e(tag, "Route ${route.routeId} Failed to delete.  Message: ${it.message}")
        }
    }

    internal var route:MutableLiveData<Route>
        get() {return _route}
        set(value) {_route = value}

    internal var routes:MutableLiveData<ArrayList<Route>>
        get() {return _routes}
        set(value) {_routes = value}

    fun getInitialDistanceCount(): MutableLiveData<Int>{
            _distance.value = initialDistance
            return _distance
    }

    private fun updateCounter(value: Int) {
        val newValue = (_distance.value ?: initialDistance) + value
        if (newValue > 0)
            _distance.value = newValue
    }

    fun onButtonClick(isIncrement: Boolean) {
        val value = if (isIncrement) STEP_COUNT_DISTANCE else -STEP_COUNT_DISTANCE
        updateCounter(value)
    }

    companion object{
        private const val STEP_COUNT_DISTANCE = 500
    }


}



