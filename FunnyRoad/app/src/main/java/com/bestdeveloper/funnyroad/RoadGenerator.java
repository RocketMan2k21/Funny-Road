package com.bestdeveloper.funnyroad;

import android.location.Location;
import android.util.Log;

import com.bestdeveloper.funnyroad.db.Repository;
import com.bestdeveloper.funnyroad.model.SnappedPoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class RoadGenerator {

    private GoogleMap mMap;

    private Location currentLocation;

    // User distance
    private int distanceInMeters;

    private Circle circle;

    private List<SnappedPoint> snappedPoints;

    public RoadGenerator(GoogleMap map, Location currentLocation, int distanceInMeters) {
        this.mMap = map;
        this.currentLocation = currentLocation;
        this.distanceInMeters = distanceInMeters/2;
    }

    // Generates a circle and markers on it
    public void generateRoute(){

        CircleOptions circleOptions = new CircleOptions()
                .center(calculateNewCoordinates(currentLocation.getLatitude(),
                        currentLocation.getLongitude(), distanceInMeters/1000, 180))
                .radius(distanceInMeters)
                .fillColor(R.color.black);

        circle = mMap.addCircle(circleOptions);
        Log.i("LOC", currentLocation.getLatitude() + ", " + currentLocation.getLongitude());

        List<LatLng> pointsToSnap = getPointsOnCircumference(circle.getRadius(), circle.getCenter(), 5);

        pointsToSnap = new Repository().getSnappedPoints(String.join(":", pointsToPath(pointsToSnap)));


    }

    private String[] pointsToPath(List<LatLng> pointsToSnap) {
        String[] path = new String[pointsToSnap.size()*2];

        for(LatLng coord: pointsToSnap){
            int index = pointsToSnap.indexOf(coord);
            path[index] = String.valueOf(coord.latitude);
            path[++index] = String.valueOf(coord.longitude);
        }

        return path;
    }

    // calcute centre of circle in given distance away from current user location
    private LatLng calculateNewCoordinates(double lat, double lon, double distanceKm, double bearingDegrees) {

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

    private List<LatLng> getPointsOnCircumference(final double radius, final LatLng center, int numOfPoints){

        double slice = 360 / numOfPoints;
        ArrayList<LatLng> lngArrayList = new ArrayList<>();

        Log.i("CNT", center.latitude + ":" + center.longitude);

        for (int i = 0; i < numOfPoints; i++)
        {
            double angle = slice * i;

            lngArrayList.add(calculateNewCoordinates(center.latitude, center.longitude, radius/1000, angle));

            mMap.addMarker(new MarkerOptions().position(lngArrayList.get(i))
                    .title("Marker" + i));

            Log.i("CRL", "point " + (i + 1) + lngArrayList.get(i).latitude + "," + lngArrayList.get(i).longitude);
        }

        return lngArrayList;

    }



}
