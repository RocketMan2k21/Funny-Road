package com.bestdeveloper.funnyroad.activity

import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.bestdeveloper.funnyroad.R
import com.bestdeveloper.funnyroad.fragments.MapFragment
import com.bestdeveloper.funnyroad.fragments.MapViewModel
import com.bestdeveloper.funnyroad.fragments.RouteMaker
import com.bestdeveloper.funnyroad.fragments.RoutesFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.firestore.FirebaseFirestore


class MapActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    //View model'
    private var mapViewModel: MapViewModel? = null

    // Map object
    private val map: GoogleMap? = null
    private val locationPermissionGranted = false

    // Points generator to make a trip
    private val routeGenerator: RouteMaker? = null

    // FirebaseFirestore database
    var firebase: FirebaseFirestore? = null

    // the entry point to the Places API
    private val placesClient: PlacesClient? = null
    private val fusedLocationProviderClient: FusedLocationProviderClient? = null

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private val lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    // UI input
    private val inputDistanceEditTxt: EditText? = null
    var llBottomSheet: LinearLayout? = null
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    // Buttons
    var generateBtn: Button? = null

    // Navigation view
    lateinit var bottomNavigationView: BottomNavigationView

    var mapFragment = MapFragment()
    var routesFragment = RoutesFragment()
    var bottomBar: LinearLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map)

        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.buttonNavView)
        bottomNavigationView.setOnItemSelectedListener(this)
        bottomNavigationView.selectedItemId = R.id.map
    }

    private fun collapseTheBottomSheet() {
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun expandTheBottomSheet() {
        // set option for bottom sheet - expanded
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideBottomSheet() {
        // set option for bottom sheet - expanded
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun BottomSheetOnClick(view: View?) {
        if (bottomSheetBehavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) expandTheBottomSheet() else collapseTheBottomSheet()
    }

    private fun generateRoute(lastKnownLocation: Location, distance: Int) {
        routeGenerator!!.generateRoute(lastKnownLocation, distance.toDouble())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.map -> {
                supportFragmentManager.beginTransaction().replace(R.id.flFragment, mapFragment)
                    .commit()
                //collapseTheBottomSheet();
                //bottomBar.setVisibility(View.VISIBLE);
                return true
            }
            R.id.routes_saved -> {
                //hideBottomSheet();
                supportFragmentManager.beginTransaction().replace(R.id.flFragment, routesFragment)
                    .commit()
                // bottomBar.setVisibility(View.GONE);
                return true
            }
        }
        return false
    }

    fun replaceFragments(fragmentClass: Class<*>) {
        var fragment: Fragment? = null
        try {
            fragment = fragmentClass.newInstance() as Fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Insert the fragment by replacing any existing fragment
        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.flFragment, fragment!!)
            .commit()
    }

    fun navigationViewToHome(){
        bottomNavigationView.selectedItemId = R.id.map
    }

    companion object {
        private val TAG = MapActivity::class.java.simpleName
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val DEFAULT_ZOOM = 15
    }
}