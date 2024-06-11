package com.bestdeveloper.funnyroad.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
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
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MapFragment : Fragment(), OnMapReadyCallback {
    private val TAG = "MapFragment"
    private var mMap: GoogleMap? = null
    private var mapReady = false
    private var locationPermissionGranted = false
    private lateinit var mapViewModel: MapViewModel

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


        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initiateMap()

        fabButton = view.findViewById(R.id.fab) as FloatingActionButton
        fabButton!!.setOnClickListener { makeAndSaveRoute() }
    }

    private fun observeViewModel() {
        mapViewModel.route.observe(viewLifecycleOwner, Observer {
            updateRoute()
        })
    }

    private fun makeAndSaveRoute() {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.make_route_dig, null)
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(view)
        val newDistance = view.findViewById<EditText>(R.id.user_dist_etx)
        newDistance.setText("1000")
        dialogBuilder.setCancelable(false)
            .setPositiveButton("Make new") { dialog, which -> }
            .setNegativeButton("Save") { dialog, which ->
                if (isGenerated) {
                    saveRoute(currRoute)
                } else {
                    dialog.cancel()
                }
            }
        val dialog = dialogBuilder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (TextUtils.isEmpty(newDistance.text.toString())) {
                Toast.makeText(context, "Please enter a distance", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newDistance.text.toString().toInt() < 200) {
                Toast.makeText(context, "Not valid distance", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                makeNewRoute(newDistance.text.toString().toInt().toDouble())
            }
        }
    }

    private fun makeNewRoute(newDistance: Double) {
        if (routeGenerator == null) {
            routeGenerator = RouteMaker(requireActivity().application, mapViewModel, mMap!!)
        }
        routeGenerator!!.generateRoute(lastKnownLocation, newDistance)
        isGenerated = true
    }

    private fun saveRoute(route: Route) {
        mapViewModel.saveRoute(route)
    }

    private fun initiateMap() {
        activity?.let {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
            mapViewModel = ViewModelProvider(it).get(MapViewModel::class.java)
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(it)
            mapFragment?.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapReady = true
        val nightModeFlags = requireContext().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_night))
            Configuration.UI_MODE_NIGHT_NO -> mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_light))
            Configuration.UI_MODE_NIGHT_UNDEFINED -> return
        }
        updateMap()
    }

    private fun updateMap() {
        if (!mapReady) {
            return
        }
        updateLocationUI()
        deviceLocation
        observeViewModel()
    }

    private fun updateRoute() {
        if (!mapReady || mMap == null) {
            return
        }

        if (routeGenerator == null) {
            routeGenerator = RouteMaker(requireActivity().application, mapViewModel, mMap!!)
        }
        routeGenerator!!.setMap(mMap!!)
        mapViewModel.route.value?.let {
            currRoute = it
            if (routeGenerator != null) {
                isGenerated = true
                routeGenerator!!.setPath(currRoute.encodedPolyline)
                mMap!!.setOnMapLoadedCallback { routeGenerator!!.showRoad() }

                Log.i(TAG, "Route's shown")
            } else {
                Log.e(TAG, "routeGenerator is null")
            }
        } ?: run {
            Log.e(TAG, "mapViewModel or route value is null")
        }
    }

    private val deviceLocation: Unit
        get() {
            try {
                if (locationPermissionGranted) {
                    fusedLocationProviderClient?.lastLocation?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            lastKnownLocation = task.result
                            if (lastKnownLocation != null) {
                                mMap?.moveCamera(
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
                            mMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                            )
                            mMap?.uiSettings?.isMyLocationButtonEnabled = false
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        }

    private val locationPermission: Unit
        get() {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
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
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        locationPermissionGranted = false
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
                changeMyLocButtonLocation()
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

    private fun changeMyLocButtonLocation(){
        val locationButton =
            (requireView().findViewById<View>("1".toInt()).getParent() as View).findViewById<View>("2".toInt())
        val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
        locationButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.loc_butt)
        locationButton.elevation = 5f
        // position on right bottom
        rlp.addRule(RelativeLayout.BELOW, 0)
        rlp.addRule(RelativeLayout.BELOW, RelativeLayout.TRUE)
        rlp.setMargins(0, 180, 180, 0)

    }
}
