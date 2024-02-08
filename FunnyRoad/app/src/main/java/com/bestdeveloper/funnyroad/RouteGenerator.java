package com.bestdeveloper.funnyroad;

import android.app.Application;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bestdeveloper.funnyroad.api.RetrofitService;
import com.bestdeveloper.funnyroad.model.SnappedPointResult;
import com.bestdeveloper.funnyroad.viewModel.MapViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteGenerator {
    private static final String TAG = "RouteGenerator";
    public static final double EarthR = 6371e3;
    private static final int CIRCLE_DEGREE = 360;
    private static final int NUM_OF_POINTS_ON_CIRC = 97;
    private static final int DEFAULT_ZOOM = 16;
    private Application application;
    private GoogleMap mMap;

    private Location currentLocation;

    // User distance
    private double distanceInMeters;

    private double routeDistance;

    private MapViewModel mapViewModel;
    private List<LatLng> snappedPoints = new ArrayList<>();
    private PolylineOptions rectOption = new PolylineOptions();
    private Polyline routePoly;

    public RouteGenerator(Application application, MapViewModel mapViewModel, GoogleMap map) {
        this.mMap = map;
        this.application = application;
        this.mapViewModel = mapViewModel;
        rectOption.color(R.color.purple_700);

    }

    // Generates a circle and markers on it
    private void generateRouteRecursive(final double circleDegreeWalked, final double circleRadius) {
        Log.i(TAG, "Generation...");
        LatLng circleCenter = calculateNewCoordinates(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                circleRadius,
                circleDegreeWalked
        );
        List<LatLng> pointsToSnap = getPointsOnCircumference(circleRadius, currentLocation, circleCenter, NUM_OF_POINTS_ON_CIRC);

        snapPoints(String.join("|", toPath(pointsToSnap)), new SnappingPointCallback() {
            @Override
            public void SnappingPointCallback() {;
                routeDistance = calculateRouteDistance();
                showRoad();
                moveCameraToRoute(circleCenter);
                Toast.makeText(application.getApplicationContext(), "Route found Dist:" + routeDistance, Toast.LENGTH_SHORT).show();

            }
        });
    }

    // Call this method to start the recursive route generation
    public void generateRoute(Location currentLocation, double distanceInMeters) {
        this.currentLocation = currentLocation;
        this.distanceInMeters = distanceInMeters;
        if(!snappedPoints.isEmpty()) {
            snappedPoints.clear();
            routeDistance = 0;
            mMap.clear();
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                generateRouteRecursive(getRandomNumber(0, CIRCLE_DEGREE), getCircleRadius(distanceInMeters));
            }
        });

    }

    private double getRandomNumber(double min, double max) {
        return ((Math.random() * (max - min)) + min);
    }

    private double getCircleRadius(final double circumference){
        double v = circumference / (2 * Math.PI);
        Log.v(TAG, "Calc. circle radius: " + v);
        return v;
    }


    private String[] toPath(List<LatLng> pointsToSnap) {
        String[] path = new String[pointsToSnap.size()];

        for(LatLng coord: pointsToSnap){
            int index = pointsToSnap.indexOf(coord);
            path[index] = String.valueOf(coord.latitude) + ',' + coord.longitude;
        }

        return path;
    }

    // calcute centre of circle in given distance away from current user location
    private LatLng calculateNewCoordinates(double lat, double lon, double distanceInMeters, double bearingDegrees) {

        final double distanceKm = distanceInMeters/1000;
        final double  earthRadiusKm = 6371; // Radius of the Earth in kilometers

        // Convert latitude and longitude from degrees to radians
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);

        // Convert bearing from degrees to radians
        double bearingRad = Math.toRadians(bearingDegrees);

        // Calculate new latitude
        double newLatRad = Math.asin(Math.sin(latRad) * Math.cos(distanceKm / earthRadiusKm) +
                Math.cos(latRad) * Math.sin(distanceKm / earthRadiusKm) * Math.cos(bearingRad));

        // Calculate new longitude
        double newLonRad = lonRad + Math.atan2(Math.sin(bearingRad) * Math.sin(distanceKm / earthRadiusKm) * Math.cos(latRad),
                Math.cos(distanceKm / earthRadiusKm) - Math.sin(latRad) * Math.sin(newLatRad));

        // Convert back to degrees
        double newLat = Math.toDegrees(newLatRad);
        double newLon = Math.toDegrees(newLonRad);

        return new LatLng(newLat, newLon);


    }

    private List<LatLng> getPointsOnCircumference(final double radiusInMeters, final Location currentLocation, final LatLng center, int numOfPoints){
        double slice = 360 / numOfPoints;
        List<LatLng> lngArrayList = new ArrayList<>();
        LatLng startingPoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        lngArrayList.add(startingPoint);
        for (int i = 0; i < numOfPoints; i++)
        {
            double angle = slice * i;

            lngArrayList.add(calculateNewCoordinates(center.latitude, center.longitude, radiusInMeters, angle));

        }

        LatLng endPoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude() + 0.0000001);
        lngArrayList.add(endPoint);

        return lngArrayList;

    }

    public void snapPoints(String path, SnappingPointCallback  callback){
        RetrofitService.SnappingPointsService snappingPointsService = RetrofitService.getSnappingPointsService();

        Call<SnappedPointResult> call = snappingPointsService.getSnappedPoints(true, path, BuildConfig.MAPS_API_KEY);

        call.enqueue(new Callback<SnappedPointResult>() {
            @Override
            public void onResponse(@NonNull Call<SnappedPointResult> call, @NonNull Response<SnappedPointResult> response) {
                SnappedPointResult result = response.body();
                if(result != null && result.getSnappedPoints() != null ){
                    Log.i(TAG, "Response successful");
                    result.getSnappedPoints().forEach(snappedPoint -> snappedPoints.add(new LatLng(
                            snappedPoint.getLocation().getLatitude(),
                            snappedPoint.getLocation().getLongitude())));
                    callback.SnappingPointCallback();

                }else{
                    Log.e(TAG, "Retrofit: failure response" );
                }
            }

            @Override
            public void onFailure(@NonNull Call<SnappedPointResult> call, @NonNull Throwable t) {
            }
        });

    }


    public void showRoad() {

        PolylineOptions polylineOptions = new PolylineOptions(); // Create a new instance

        for (LatLng point : snappedPoints) {
            polylineOptions.add(point);
        }

        // Start/End markers
        mMap.addMarker(new MarkerOptions().position(snappedPoints.get(0)).title("Start"));
        mMap.addMarker(new MarkerOptions().position(snappedPoints.get(snappedPoints.size()-1)).title("Finish"));


        // Create a new polyline using the snapped points
        routePoly = mMap.addPolyline(polylineOptions);

        // Customize the polyline appearance if needed
        routePoly.setColor(ContextCompat.getColor(application.getApplicationContext(), R.color.purple_700));
        routePoly.setWidth(20);  // Set the polyline width in pixels



        mapViewModel.setSnappedPointsLiveData(snappedPoints);
    }

    private void moveCameraToRoute(LatLng zoomToCords) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                zoomToCords, DEFAULT_ZOOM));
    }

    public void setSnappedPoints(List<LatLng> snappedPoints) {
        this.snappedPoints = snappedPoints;
    }

    public double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }
    public double calculateRouteDistance() {
        double distance = 0;
        if(!snappedPoints.isEmpty())
            for(int i = 0; i < snappedPoints.size()-1; i++){
                double lat1 = snappedPoints.get(i).latitude;
                double lon1 = snappedPoints.get(i).longitude;
                double lat2 = snappedPoints.get(i+1).latitude;
                double lon2 = snappedPoints.get(i+1).longitude;
                double φ1 = degreesToRadians(lat1);
                double φ2 = degreesToRadians(lat2);
                double Δφ = degreesToRadians(lat2 - lat1);
                double Δλ = degreesToRadians(lon2 - lon1);
                double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                        Math.cos(φ1) * Math.cos(φ2) *
                                Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                distance += EarthR * c;
            }
        Log.d(TAG, "route distance in meters: " + distance);
        return distance;
    }


}
