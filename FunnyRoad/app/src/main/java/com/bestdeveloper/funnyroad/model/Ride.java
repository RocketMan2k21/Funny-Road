package com.bestdeveloper.funnyroad.model;

public class Ride {
    private String rideID;
    private String rideName;
    private double duration;
    private double mileage;
    private double startTime;
    private double endTime;
    private RideType rideType;

    public Ride(String rideID, String rideName, double duration, double mileage, double startTime, double endTime, RideType rideType) {
        this.rideID = rideID;
        this.rideName = rideName;
        this.duration = duration;
        this.mileage = mileage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.rideType = rideType;
    }

    public String getRideID() {
        return rideID;
    }

    public void setRideID(String rideID) {
        this.rideID = rideID;
    }

    public String getRideName() {
        return rideName;
    }

    public void setRideName(String rideName) {
        this.rideName = rideName;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getMileage() {
        return mileage;
    }

    public void setMileage(double mileage) {
        this.mileage = mileage;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public RideType getRideType() {
        return rideType;
    }

    public void setRideType(RideType rideType) {
        this.rideType = rideType;
    }
}
