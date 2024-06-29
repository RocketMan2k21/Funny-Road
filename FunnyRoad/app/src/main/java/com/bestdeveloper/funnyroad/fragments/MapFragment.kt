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
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.databinding.FragmentMapBinding
import com.bestdeveloper.funnyroad.databinding.MakeRouteDigBinding
import com.bestdeveloper.funnyroad.model.Route
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MapFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mapReady = false
    private var _locationPermissionGranted = false
    private lateinit var mapViewModel: MapViewModel

    // Points generator to make a trip
    private var routeGenerator: RouteMaker? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    // the entry point to the Places API
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var isGenerated = false
    private var cameraPosition: CameraPosition? = null

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    private var fabButton: FloatingActionButton? = null
    private lateinit var currRoute: Route

    private  var _dialogueBinding: MakeRouteDigBinding? = null
    private val dialogueBinding get() = _dialogueBinding!!

    private var _mapBinding :  FragmentMapBinding? = null
    private val mapBinding get() = _mapBinding!!

    // Progress bar
    private lateinit var  mapProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getLocationPermission()
        _mapBinding = FragmentMapBinding.inflate(inflater, container, false)
        return mapBinding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mapProgressBar = mapBinding.mapFrDeterminateBar
        mapProgressBar.visibility = View.VISIBLE
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment?.getMapAsync(this)

        fabButton = view.findViewById(R.id.fab) as FloatingActionButton
        fabButton!!.setOnClickListener { makeAndSaveRoute() }
        val locationButton = view.findViewById<View>(R.id.location_button)
        locationButton.setOnClickListener { getDeviceLocation(true) }

        //Progress bar

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _dialogueBinding = null
    }

    private fun observeViewModel() {
        mapViewModel.route.observe(viewLifecycleOwner, Observer {
            updateRoute()
        })
    }

    private fun makeAndSaveRoute() {
        val layoutInflater = LayoutInflater.from(context)
        _dialogueBinding = MakeRouteDigBinding.inflate(layoutInflater)

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
        dialogBuilder.setView(dialogueBinding.root)

        var dialogDistanceEditTxt = dialogueBinding.dialogDistanceEditTxt
        val initialDistanceCount = mapViewModel.getInitialDistanceCount()
        initialDistanceCount.observe(viewLifecycleOwner, Observer {
            dialogDistanceEditTxt.setText("" + it)
        })


        dialogueBinding.dialogPlusImgButton.setOnClickListener(View.OnClickListener {
            mapViewModel.onButtonClick(true)
        })
        dialogueBinding.dialogMinusImageBtn.setOnClickListener(View.OnClickListener {
            mapViewModel.onButtonClick(false)
        })

        val dialog = dialogBuilder.create()

        dialogueBinding.dialogSaveOrCancelBtn.apply {
            text = if (isGenerated) "Save" else "Cancel"
            setOnClickListener {
                if (isGenerated) {
                    saveRoute(currRoute)
                    dialog.cancel()
                } else {
                    dialog.cancel()
                }
            }
        }
        dialogueBinding.dialogCloseBtn.setOnClickListener{
            dialog.cancel()
        }
        dialog.show()
        dialogueBinding.dialogGenerateBtn.setOnClickListener {
            if (TextUtils.isEmpty(dialogDistanceEditTxt.text.toString())) {
                Toast.makeText(context, "Please enter a distance", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dialogDistanceEditTxt.text.toString().toInt() < 200) {
                Toast.makeText(context, "Not valid distance", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                makeNewRoute(dialogDistanceEditTxt.text.toString().toInt().toDouble())
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


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapReady = true
        mapProgressBar.visibility = View.GONE
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
        getDeviceLocation(false)
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

                Log.i(Companion.TAG, "Route's shown")
            } else {
                Log.e(Companion.TAG, "routeGenerator is null")
            }
        } ?: run {
            Log.e(Companion.TAG, "mapViewModel or route value is null")
        }
    }

    private fun getDeviceLocation(forButton : Boolean){
            try {
                if (_locationPermissionGranted) {
                    fusedLocationProviderClient?.lastLocation?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            lastKnownLocation = task.result
                            if (lastKnownLocation != null) {
                                if (!forButton)
                                    mMap?.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                lastKnownLocation!!.latitude,
                                                lastKnownLocation!!.longitude
                                            ), DEFAULT_ZOOM.toFloat()
                                        )
                                    )
                                else{
                                    mMap?.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                lastKnownLocation!!.latitude,
                                                lastKnownLocation!!.longitude
                                            ), mMap?.cameraPosition!!.zoom
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        }

    private fun useDefaultLocation() {
        Toast.makeText(requireContext(), "Current location is unavailable. Please allow location permission.", Toast.LENGTH_LONG).show()
        Log.d(TAG, "Current location is null. Using defaults.")
        mMap?.moveCamera(CameraUpdateFactory
            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                locationPermissionGranted = true
                getDeviceLocation(false)
            }
            else {
                useDefaultLocation()
            }
            updateLocationUI()
        }
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
       _locationPermissionGranted = false
        when (requestCode) {
            MapFragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }

    fun updateLocationUI() {
        if (mMap == null) {
            return
        }
            try {
            if (_locationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = false

            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }
    internal var locationPermissionGranted : Boolean
        get() { return _locationPermissionGranted}
        set(value) {_locationPermissionGranted = value}

    companion object {
        private const val DEFAULT_ZOOM = 15
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        const val TAG =  "MapFragment"
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }


}
