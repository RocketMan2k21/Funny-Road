package com.bestdeveloper.funnyroad.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.model.Route
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapFragment : Fragment() {
    private val TAG = Fragment::class.java.simpleName
    private var mMap: GoogleMap? = null
    private var mapReady = false
    private var locationPermissionGranted = false
    private var mapViewModel: MapViewModel? = null

    // Points generator to make a trip
    private var routeGenerator: RouteMaker? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    // the entry point to the Places API
    private val placesClient: PlacesClient? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var isGenerated = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    private var fabButton: FloatingActionButton? = null
    private lateinit var currRoute: Route

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mapFragment!!.getMapAsync { googleMap: GoogleMap? ->
            mMap = googleMap
            mapReady = true
            updateMap()
        }

        mapViewModel!!.route.observe(viewLifecycleOwner, Observer {
            currRoute = it
        })

        fabButton = view.findViewById(R.id.fab) as FloatingActionButton
        fabButton!!.setOnClickListener(View.OnClickListener { makeAndSaveRoute() })
    }

    private fun makeAndSaveRoute() {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.make_route_dig, null)
        val dialogBilder = AlertDialog.Builder(context)
        dialogBilder.setView(view)
        val newDistance = view.findViewById<EditText>(R.id.user_dist_etx)
        newDistance.setText("1000")
        dialogBilder.setCancelable(false)
            .setPositiveButton("Make new") { dialog, which -> }
            .setNegativeButton("Save") { dialog, which ->
                if (isGenerated) {
                    saveRoute(currRoute)
                } else {
                    dialog.cancel()
                }
            }
        val dialog = dialogBilder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(newDistance.text.toString())) {
                Toast.makeText(context, "Please enter a distance", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (newDistance.text.toString().toInt() < 200) {
                Toast.makeText(context, "Not valid distance", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                makeNewRoute(newDistance.text.toString().toInt().toDouble())
            }
        })
    }

    private fun makeNewRoute(newDistance: Double) {
        routeGenerator!!.generateRoute(lastKnownLocation, newDistance)
        isGenerated = true
    }

    private fun saveRoute(route: Route) {
        mapViewModel!!.saveRoute(route)
    }

    private fun updateMap() {
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        deviceLocation

        // Initialize routeGenerator if not already initialized
        if (routeGenerator == null) {
            routeGenerator = RouteMaker(requireActivity().application, mapViewModel!!, mMap!!)
        }

        // Ensure mapViewModel and currRoute are not null before accessing their properties
        if (mapViewModel != null && mapViewModel!!.route.value != null) {
            currRoute = mapViewModel!!.route.value!!

            // Check if currRoute and routeGenerator are not null before accessing their properties or methods
            if (routeGenerator != null) {
                // Set the path and show the road
                isGenerated = true
                routeGenerator!!.setPath(currRoute!!.encodedPolyline)
                routeGenerator!!.showRoad()
            } else {
                // Handle null currRoute or routeGenerator
                Log.e(TAG, "currRoute or routeGenerator is null")
            }
        } else {
            // Handle null mapViewModel or route value
            Log.e(TAG, "mapViewModel or route value is null")
        }
    }


    /*
            * Get the best and most recent location of the device, which may be null in rare
            * cases when a location is not available.
            */
    private val deviceLocation: Unit
        private get() {
            /*
                * Get the best and most recent location of the device, which may be null in rare
                * cases when a location is not available.
                */
            try {
                if (locationPermissionGranted) {
                    val locationResult = fusedLocationProviderClient!!.lastLocation
                    locationResult.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.result
                            if (lastKnownLocation != null) {
                                mMap!!.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude
                                        ), DEFAULT_ZOOM.toFloat()
                                    )
                                )
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                            mMap!!.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                            )
                            mMap!!.uiSettings.isMyLocationButtonEnabled = false
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        }

    /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
    private val locationPermission: Unit
        private get() {
            /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionGranted = true
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        if (requestCode
            == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        ) {
            if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionGranted = true
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap!!.isMyLocationEnabled = false
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                locationPermission
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}