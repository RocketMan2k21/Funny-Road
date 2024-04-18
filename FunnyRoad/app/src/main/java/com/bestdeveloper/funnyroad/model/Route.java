package com.bestdeveloper.funnyroad.model;


import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Route {
    private List<LatLng> polyline;
    private double distance;
    private RideType rideType;
    private RouteType routeType;

    public Route(List<LatLng> polyline, double distance, RideType rideType, RouteType routeType) {
        this.polyline = polyline;
        this.distance = distance;
        this.rideType = rideType;
        this.routeType = routeType;
    }

    private String substringLatLng(LatLng point){
        return point.latitude + "," + point.longitude;
    }

}
