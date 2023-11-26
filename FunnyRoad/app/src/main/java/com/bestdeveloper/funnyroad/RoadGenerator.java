package com.bestdeveloper.funnyroad;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bestdeveloper.funnyroad.api.RetrofitService;
import com.bestdeveloper.funnyroad.db.Repository;
import com.bestdeveloper.funnyroad.model.SnappedPointResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoadGenerator {
    private Application application;
    private GoogleMap mMap;

    private Location currentLocation;

    // User distance
    private int distanceInMeters;

    private Circle circle;

    private List<LatLng> snappedPoints = new ArrayList<>();

    public RoadGenerator(Application application, GoogleMap map, Location currentLocation, int distanceInMeters) {
        this.mMap = map;
        this.currentLocation = currentLocation;
        this.distanceInMeters = distanceInMeters/2;
        this.application = application;

    }

    // Generates a circle and markers on it
    public void generateRoute(){

        final LatLng circleCenter = calculateNewCoordinates(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                distanceInMeters,
                -90

        );

        final double circleRadius = distanceInMeters;

        Log.i("LOC", currentLocation.getLatitude() + ", " + currentLocation.getLongitude());

        List<LatLng> pointsToSnap = getPointsOnCircumference(circleRadius, currentLocation, circleCenter, 50);
        snapPoints(String.join("|", toPath(pointsToSnap)));



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
        final LatLng currentLocationPoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        double slice = 360 / numOfPoints;
        List<LatLng> lngArrayList = new ArrayList<>();

        lngArrayList.add(currentLocationPoint);
        for (int i = 0; i < numOfPoints; i++)
        {
            double angle = slice * i;

            lngArrayList.add(calculateNewCoordinates(center.latitude, center.longitude, radiusInMeters, angle));

           // mMap.addMarker(new MarkerOptions().position(lngArrayList.get(i)));

        }
        lngArrayList.add(currentLocationPoint);
        return lngArrayList;

    }


    public void snapPoints(String path){
        RetrofitService.SnappingPointsService snappingPointsService = RetrofitService.getSnappingPointsService();

        Call<SnappedPointResult> call = snappingPointsService.getSnappedPoints(true, path, BuildConfig.MAPS_API_KEY);

        call.enqueue(new Callback<SnappedPointResult>() {
            @Override
            public void onResponse(@NonNull Call<SnappedPointResult> call, @NonNull Response<SnappedPointResult> response) {
                SnappedPointResult result = response.body();
                if(result != null && result.getSnappedPoints() != null ){
                    Log.i("REP", "Response successful");
                    result.getSnappedPoints().forEach(snappedPoint -> snappedPoints.add(new LatLng(
                            snappedPoint.getLocation().getLatitude(),
                            snappedPoint.getLocation().getLongitude())));
                    showRoad(snappedPoints);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SnappedPointResult> call, @NonNull Throwable t) {
                Log.e("REP", "Retrofit: failure response" );
            }
        });

    }


    public void showRoad(List<LatLng> snappedPoints){
        PolylineOptions rectOption = new PolylineOptions();

        for(LatLng point: snappedPoints){
            rectOption.add(point);
            mMap.addMarker(new MarkerOptions().position(point));
        }
         mMap.addPolyline(rectOption);
    }

}
