package com.bestdeveloper.funnyroad.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bestdeveloper.funnyroad.R;
import com.bestdeveloper.funnyroad.RouteMaker;
import com.bestdeveloper.funnyroad.model.Route;
import com.bestdeveloper.funnyroad.viewModel.MapViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment {

    private final String TAG = Fragment.class.getSimpleName();

    private static final int DEFAULT_ZOOM = 15;


    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;
    private boolean mapReady = false;
    private boolean locationPermissionGranted;

    private MapViewModel mapViewModel;

    // Points generator to make a trip

    private RouteMaker routeGenerator;

    private LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    // the entry point to the Places API
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private boolean isGenerated;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    private FloatingActionButton fabButton;
    private String currRoute;


    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);

        mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        mapFragment.getMapAsync(googleMap -> {
            mMap = googleMap;
            mapReady = true;
            updateMap();
        });

        fabButton = rootView.findViewById(R.id.fab);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeAndSaveRoute();
            }
        });


        return rootView;
    }

    private void makeAndSaveRoute() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.make_route_dig, null);

        AlertDialog.Builder dialogBilder = new AlertDialog.Builder(getContext());
        dialogBilder.setView(view);

        EditText newDistance = view.findViewById(R.id.user_dist_etx);
        newDistance.setText("1000");


        dialogBilder.setCancelable(false)
                .setPositiveButton("Make new", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isGenerated && routeGenerator.getRoutePath() != null) {
                            saveRoute(routeGenerator.getRoutePath());
                        } else {
                            dialog.cancel();
                        }
                    }
                });


        AlertDialog dialog = dialogBilder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(newDistance.getText().toString())) {
                    Toast.makeText(getContext(), "Please enter a distance", Toast.LENGTH_SHORT).show();
                    return;
                }if(Integer.parseInt(newDistance.getText().toString()) < 200){
                    Toast.makeText(getContext(), "Not valid distance", Toast.LENGTH_SHORT).show();

                } else {
                    dialog.dismiss();
                    makeNewRoute(Integer.parseInt(newDistance.getText().toString()));
                }

            }
        });
    }

    private void makeNewRoute(double newDistance) {
        routeGenerator.generateRoute(lastKnownLocation, newDistance);
        isGenerated = true;
    }

    private void saveRoute(final String path){
        mapViewModel.saveRoute(path);
    }

    private void updateMap() {
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();


        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        routeGenerator = new RouteMaker(getActivity().getApplication(), mapViewModel, mMap);
        currRoute = mapViewModel.getPath().getValue();

        //Check if there was an generated route
        if(currRoute != null){
            routeGenerator.setPath(currRoute).showRoad();
            isGenerated = true;
        }else{
            isGenerated = false;
        }

    }



    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission(){
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if(ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        }else{
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if(requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                locationPermissionGranted = true;
            }else{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }

        }

    }

    public void updateLocationUI(){
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);


            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }

    }

}