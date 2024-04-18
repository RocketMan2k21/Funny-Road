package com.bestdeveloper.funnyroad;

import android.app.Application;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bestdeveloper.funnyroad.api.RetrofitService;
import com.bestdeveloper.funnyroad.model.SnappedPointResult;
import com.bestdeveloper.funnyroad.service.SnappingPointCallback;
import com.bestdeveloper.funnyroad.viewModel.MapViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteMaker {
    private static final String TAG = "RouteGenerator";
    public static final double EarthR = 6371e3;
    private static final int CIRCLE_DEGREE = 360;
    private static final int NUM_OF_POINTS_ON_CIRC = 90;
    private static final int DEFAULT_ZOOM = 16;
    private Application application;
    private GoogleMap mMap;
    private RequestQueue requestQueue;

    private Location currentLocation;

    // User distance
    private double distanceInMeters;

    private double routeDistance;

    private MapViewModel mapViewModel;
    private List<LatLng> snappedPoints = new ArrayList<>();
    private PolylineOptions rectOption = new PolylineOptions();
    private Polyline routePoly;


    public RouteMaker(Application application, MapViewModel mapViewModel, GoogleMap map) {
        this.mMap = map;
        this.application = application;
        this.mapViewModel = mapViewModel;
        rectOption.color(R.color.purple_700);
        requestQueue = Volley.newRequestQueue(application.getApplicationContext());

    }

    // Generates a circle and markers on it
    private void generateRouteRecursive(final double circleDegreeWalked, final double circleRadius) {
        Log.i(TAG, "Generation...");
        LatLng circleCenter = calculateNewCoordinates(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                circleRadius,
                circleDegreeWalked);
        List<LatLng> pointsToSnap = getPointsOnCircumference(circleRadius, currentLocation, circleCenter, NUM_OF_POINTS_ON_CIRC);

        snapPoints(String.join("|", toPath(pointsToSnap)), new SnappingPointCallback() {
            @Override
            public void SnappingPointCallback() {
                routeDistance = calculateRouteDistance();
                snappedPoints.add(snappedPoints.get(0));
                volleyResponse();
                moveCameraToRoute(circleCenter);
                Toast.makeText(application.getApplicationContext(), "Route found Dist:" + routeDistance, Toast.LENGTH_SHORT).show();

            }
        });
    }

    // Call this method to start the recursive route generation
    public void generateRoute(Location currentLocation, double distanceInMeters) {
        this.currentLocation = currentLocation;
        this.distanceInMeters = distanceInMeters;
        if (!snappedPoints.isEmpty()) {
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

    private double getCircleRadius(final double circumference) {
        double v = circumference / (2 * Math.PI);
        Log.v(TAG, "Calc. circle radius: " + v);
        return v;
    }


    private String[] toPath(List<LatLng> pointsToSnap) {
        String[] path = new String[pointsToSnap.size()];

        for (LatLng coord : pointsToSnap) {
            int index = pointsToSnap.indexOf(coord);
            path[index] = String.valueOf(coord.latitude) + ',' + coord.longitude;
        }

        return path;
    }

    // calcute centre of circle in given distance away from current user location
    private LatLng calculateNewCoordinates(double lat, double lon, double distanceInMeters, double bearingDegrees) {

        final double distanceKm = distanceInMeters / 1000;
        final double earthRadiusKm = 6371; // Radius of the Earth in kilometers

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

    private List<LatLng> getPointsOnCircumference(final double radiusInMeters, final Location currentLocation, final LatLng center, int numOfPoints) {
        double slice = 360 / numOfPoints;
        List<LatLng> lngArrayList = new ArrayList<>();
        for (int i = 0; i < numOfPoints; i++) {
            double angle = slice * i;

            lngArrayList.add(calculateNewCoordinates(center.latitude, center.longitude, radiusInMeters, angle));


        }

        return lngArrayList;

    }

    private void showPoints(List<LatLng> points) {
        mMap.clear();
        PolylineOptions polylineOptions = new PolylineOptions();
        for (LatLng point : points) {
            mMap.addMarker(new MarkerOptions().position(point).title("Marker " + points.indexOf(point)));
            polylineOptions.add(point);
        }
        mMap.addPolyline(polylineOptions);
    }

    public void snapPoints(String path, SnappingPointCallback callback) {
        RetrofitService.SnappingPointsService snappingPointsService = RetrofitService.getSnappingPointsService();

        Call<SnappedPointResult> call = snappingPointsService.getSnappedPoints(true, path, BuildConfig.MAPS_API_KEY);

        call.enqueue(new Callback<SnappedPointResult>() {
            @Override
            public void onResponse(@NonNull Call<SnappedPointResult> call, @NonNull Response<SnappedPointResult> response) {
                SnappedPointResult result = response.body();
                if (result != null && result.getSnappedPoints() != null) {
                    Log.i(TAG, "Response successful");
                    result.getSnappedPoints().forEach(snappedPoint -> snappedPoints.add(new LatLng(
                            snappedPoint.getLocation().getLatitude(),
                            snappedPoint.getLocation().getLongitude())));
                    callback.SnappingPointCallback();

                } else {
                    Log.e(TAG, "Retrofit: failure response");
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

        // Create a new polyline using the snapped points
        routePoly = mMap.addPolyline(polylineOptions);

        // Customize the polyline appearance if needed
        routePoly.setColor(ContextCompat.getColor(application.getApplicationContext(), R.color.purple_700));
        routePoly.setWidth(20);  // Set the polyline width in pixels

        mapViewModel.setRouteMakerLiveData(PolyUtil.encode(snappedPoints));

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

        if (!snappedPoints.isEmpty())
            for (int i = 0; i < snappedPoints.size() - 1; i++) {
                double lat1 = snappedPoints.get(i).latitude;
                double lon1 = snappedPoints.get(i).longitude;
                double lat2 = snappedPoints.get(i + 1).latitude;
                double lon2 = snappedPoints.get(i + 1).longitude;
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

    private int getIndexOfTheNearestPoint(double lat, double lon) {
        double MinDistance = Double.MAX_VALUE; // Initialize to a large value
        int indexOfTheNearestPoint = -1; // Initialize to -1 indicating no point found
        double φ1 = degreesToRadians(lat); // Calculate φ1 (latitude of target point)

        if (!snappedPoints.isEmpty()) {
            for (int i = 0; i < snappedPoints.size(); i++) {
                double lat1 = snappedPoints.get(i).latitude;
                double lon1 = snappedPoints.get(i).longitude;

                double φ2 = degreesToRadians(lat1);
                double Δφ = degreesToRadians(lat1 - lat);
                double Δλ = degreesToRadians(lon1 - lon);

                double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                        Math.cos(φ1) * Math.cos(φ2) *
                                Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double distance = EarthR * c;

                if (distance < MinDistance) {
                    MinDistance = distance; // Update minimum distance
                    indexOfTheNearestPoint = i; // Update index of nearest point
                }
            }
        }

        return indexOfTheNearestPoint;
    }


    private void volleyResponse() {
        List<LatLng> wayPoints = new ArrayList<>();
        wayPoints.add(snappedPoints.get(0));
        wayPoints.add(snappedPoints.get(snappedPoints.size() - 1));
        volleyRequest(wayPoints);
    }

    private List<List<LatLng>> volleyRequest(List<LatLng> wayPoints) {
        List<List<LatLng>> path = new ArrayList<>();
        LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        int indexOfTheNearestPoint = getIndexOfTheNearestPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        String urlDirections = "https://maps.googleapis.com/maps/api/directions/json" +
                "?destination=" + substringLatLng(snappedPoints.get(indexOfTheNearestPoint)) +
                "&origin=" + substringLatLng(currentLocationLatLng) +
                "&key=" + BuildConfig.MAPS_API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlDirections, null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i(TAG, "Volley: successful");
                            JSONArray routes = response.getJSONArray("routes");
                            JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                            JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");
                            for (int i = 0; i < steps.length(); i++) {
                                String points = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                                path.add(PolyUtil.decode(points));
                                snappedPoints.addAll(0, path.get(i));
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        showRoad();
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonObjectRequest);

        return null;
    }

    private String substringLatLng(LatLng point) {
        return point.latitude + "," + point.longitude;
    }

    public RouteMaker setPath(String decoded_path){
        snappedPoints = PolyUtil.decode(decoded_path);
        return this;
    }

    public String getRoutePath() {
        return PolyUtil.encode(snappedPoints);
    }
}