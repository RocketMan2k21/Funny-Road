package com.bestdeveloper.funnyroad;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bestdeveloper.funnyroad.api.RetrofitService;
import com.bestdeveloper.funnyroad.model.RideType;
import com.bestdeveloper.funnyroad.model.SnappedPointResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoadGenerator {
    public static double WALKING_SPEED_KM_PER_HOUR = 5;
    public static final double R = 6371e3; // metres
    private double routeDisBias;
    private Application application;
    private GoogleMap mMap;

    private Location currentLocation;

    // User distance
    private int distanceInMeters;

    private List<LatLng> snappedPoints = new ArrayList<>();
    private double routeDistance = 0;

    public RoadGenerator(Application application, GoogleMap map, Location currentLocation, int distanceInMeters) {
        this.mMap = map;
        this.currentLocation = currentLocation;
        this.distanceInMeters = distanceInMeters;
        this.application = application;
        routeDisBias = distanceInMeters * 0.1;

    }

    // Generates a circle and markers on it
    public void generateRoute(){
        final double radiusStep = 50;
        double circleDegreeWalked = 0;
        final int numberOfPoints = 50;
        int circleRadius = 200;

        while( routeDistance < distanceInMeters - routeDisBias || routeDistance > distanceInMeters + routeDisBias){

            LatLng circleCenter = calculateNewCoordinates(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    circleRadius,
                    circleDegreeWalked

            );
            //Log.i("LOC", currentLocation.getLatitude() + ", " + currentLocation.getLongitude());

            List<LatLng> pointsToSnap = getPointsOnCircumference(circleRadius, currentLocation, circleCenter, numberOfPoints);
            snapPoints(String.join("|", toPath(pointsToSnap)));

            snappedPoints.clear();
            circleRadius+=radiusStep;

        }

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
        //lngArrayList.add(currentLocationPoint);
        for (int i = 0; i < numOfPoints; i++)
        {
            double angle = slice * i;

            lngArrayList.add(calculateNewCoordinates(center.latitude, center.longitude, radiusInMeters, angle));

            //Log.i("RoadGenerator", "Circumference point " + i + ": " + lngArrayList.get(i));
           // mMap.addMarker(new MarkerOptions().position(lngArrayList.get(i)).title("marker" + i ));
        }
        //lngArrayList.add(currentLocationPoint);
        return lngArrayList;

    }


    private void snapPoints(String path){
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
                    routeDistance = calculateRouteDistance();

                }
            }

            @Override
            public void onFailure(@NonNull Call<SnappedPointResult> call, @NonNull Throwable t) {
                Log.e("REP", "Retrofit: failure response" );
            }
        });

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



    private void showRoad(List<LatLng> snappedPoints){

        PolylineOptions rectOption = new PolylineOptions();
        mMap.clear();
        for(LatLng point: snappedPoints){
            //Log.i("RoadGenerator", "snapped point: " + point.latitude + ',' + point.longitude   );
            rectOption.add(point);
            //mMap.addMarker(new MarkerOptions().position(point));
        }
         mMap.addPolyline(rectOption);
    }

    public double getRouteDistance() {
        return routeDistance;
    }


    public double distanceBasedOnTime(double minutes, RideType rideType){
        switch (rideType){
            default:
                return 0;

        }
    }
}
