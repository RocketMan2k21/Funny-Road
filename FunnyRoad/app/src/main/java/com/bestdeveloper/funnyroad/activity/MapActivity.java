package com.bestdeveloper.funnyroad.activity;

import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bestdeveloper.funnyroad.fragments.RouteMaker;
import com.bestdeveloper.funnyroad.R;
import com.bestdeveloper.funnyroad.fragments.MapFragment;
import com.bestdeveloper.funnyroad.fragments.RoutesFragment;
import com.bestdeveloper.funnyroad.fragments.MapViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapActivity extends AppCompatActivity
    implements NavigationBarView.OnItemSelectedListener {

    private static final String TAG = MapActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    //View model'
    private MapViewModel mapViewModel;


    // Map object
    private GoogleMap map;
    private boolean locationPermissionGranted;

    // Points generator to make a trip

    private RouteMaker routeGenerator;

    // FirebaseFirestore database
    FirebaseFirestore firebase;

    // the entry point to the Places API
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);


    // UI input
    private EditText inputDistanceEditTxt;
    LinearLayout llBottomSheet;
    BottomSheetBehavior bottomSheetBehavior;

    // Buttons
    Button generateBtn;

    // Navigation view
    BottomNavigationView bottomNavigationView;
    MapFragment mapFragment = new MapFragment();
    RoutesFragment routesFragment = new RoutesFragment();

    LinearLayout bottomBar;

    public MapActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        bottomNavigationView = findViewById(R.id.buttonNavView);
        bottomNavigationView.setOnItemSelectedListener(this);

        bottomNavigationView.setSelectedItemId(R.id.map);

        routeGenerator = new RouteMaker(getApplication(), mapViewModel, map);

//        bottomBar = (LinearLayout) findViewById(R.id.bottomAppBar);

        // Expand the bottom sheet
//        llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
//        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

//        inputDistanceEditTxt = findViewById(R.id.user_dist_etx);
//        inputDistanceEditTxt.setText("1000");


//        generateBtn= findViewById(R.id.generate_btn);
//        generateBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "Generator button clicked");
//                String distanceString = inputDistanceEditTxt.getText().toString();
//                if(!TextUtils.isEmpty(distanceString)) {
//                    final int distance = Integer.parseInt(distanceString);
//                    Log.d(TAG, "inp. dist. : " + distance);
//                    if (distance > 0){
//                        generateRoute(lastKnownLocation, distance);
//                    }
//                    else
//                        Toast.makeText(MapActivity.this, "Distance value is less than 1 meter", Toast.LENGTH_SHORT).show();
//                }
//                else
//                    Toast.makeText(MapActivity.this, "Distance value is not correct. Try Again!", Toast.LENGTH_SHORT).show();
//            }
//        });
//



    }




    private void collapseTheBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void expandTheBottomSheet() {
        // set option for bottom sheet - expanded
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void hideBottomSheet() {
        // set option for bottom sheet - expanded
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void BottomSheetOnClick(View view){
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
            expandTheBottomSheet();
        else
            collapseTheBottomSheet();
    }

    private void generateRoute(Location lastKnownLocation, int distance) {
        routeGenerator.generateRoute(lastKnownLocation, distance);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch ((item.getItemId())){
            case  R.id.map:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, mapFragment).commit();
                //collapseTheBottomSheet();
                //bottomBar.setVisibility(View.VISIBLE);
                return true;
            case  R.id.routes_saved:
                //hideBottomSheet();
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, routesFragment).commit();
               // bottomBar.setVisibility(View.GONE);
                return true;
        }
        return false;
    }
}