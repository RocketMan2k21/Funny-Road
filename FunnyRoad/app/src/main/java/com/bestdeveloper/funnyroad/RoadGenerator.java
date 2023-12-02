package com.bestdeveloper.funnyroad;

import android.app.Application;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

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
    public static final double R = 6371e3;
    private Application application;
    private GoogleMap mMap;

    private Location currentLocation;

    // User distance
    private final int distanceInMeters;

    private double routeDistance;
    private double circleRadius;
    private double routeDisBias;

    private List<LatLng> snappedPoints = new ArrayList<>();

    public RoadGenerator(Application application, GoogleMap map, Location currentLocation, int distanceInMeters) {
        this.mMap = map;
        this.currentLocation = currentLocation;
        this.distanceInMeters = distanceInMeters;
        this.application = application;

    }

    // Generates a circle and markers on it
    private void generateRouteRecursive(final double circleDegreeWalked, final int circleRadius) {
        if (routeDistance >= distanceInMeters) {
            Toast.makeText(application.getApplicationContext(), "Route found; Dist:" +  routeDistance, Toast.LENGTH_SHORT).show();
            return;  // Stop recursion when the condition is met
        }

        LatLng circleCenter = calculateNewCoordinates(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                circleRadius,
                circleDegreeWalked
        );

        List<LatLng> pointsToSnap = getPointsOnCircumference(circleRadius, currentLocation, circleCenter, 50);

        snapPoints(String.join("|", toPath(pointsToSnap)), new SnappingPointCallback() {
            @Override
            public void SnappingPointCallback() {
                Log.i("RoadGenerator", "snapPoints size: " + snappedPoints.size());
                routeDistance = calculateRouteDistance();
                showRoad();
                snappedPoints.clear();

                // Call the next iteration with updated parameters
                generateRouteRecursive(circleDegreeWalked, circleRadius + 50);
            }
        });
    }

    // Call this method to start the recursive route generation
    public void generateRoute() {
        routeDisBias = distanceInMeters*0.1;
        generateRouteRecursive(90, 200);
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

        //lngArrayList.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        for (int i = 0; i < numOfPoints; i++)
        {
            double angle = slice * i;

            lngArrayList.add(calculateNewCoordinates(center.latitude, center.longitude, radiusInMeters, angle));

           // mMap.addMarker(new MarkerOptions().position(lngArrayList.get(i)));

        }
        //lngArrayList.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        Log.i("RoadGenerator", "points on circumference: " + lngArrayList);
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
                    Log.i("RoadGenerator", "Response successful");
                    result.getSnappedPoints().forEach(snappedPoint -> snappedPoints.add(new LatLng(
                            snappedPoint.getLocation().getLatitude(),
                            snappedPoint.getLocation().getLongitude())));
                    callback.SnappingPointCallback();

                }else{
                    Log.e("REP", "Retrofit: failure response" );
                }
            }

            @Override
            public void onFailure(@NonNull Call<SnappedPointResult> call, @NonNull Throwable t) {
            }
        });

    }


    public void showRoad(){
        PolylineOptions rectOption = new PolylineOptions();

        for(LatLng point: snappedPoints){
            rectOption.add(point);
            Log.i("RoadGenerator", "Point" + snappedPoints.indexOf(point) + ": " +
                    + point.longitude + "," + point.latitude);


        }
         mMap.addPolyline(rectOption);
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
                distance += R * c;
            }
        Log.i("RoadGenerator", "route distance in meters: " + distance);
        return distance;
    }



}
