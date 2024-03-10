package com.bestdeveloper.funnyroad.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.bestdeveloper.funnyroad.db.Repository;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class MapViewModel extends AndroidViewModel {
    private Repository repository;
    private MutableLiveData<List<LatLng>> snappedPoints = new MutableLiveData<>();

    // points that connects snappedPoints with current user's location
    private MutableLiveData<List<List<LatLng>>> directionsPoints = new MutableLiveData<>();

    private MutableLiveData<GoogleMap> map = new MutableLiveData<>();
    public MapViewModel(Application application){
        super(application);

        repository = new Repository();
    }

    public void setMap(GoogleMap Mmap) {
        map.setValue(Mmap);
    }


    public MutableLiveData<GoogleMap> getMap() {
        return map;
    }

    public MutableLiveData<List<LatLng>> getSnappedPoints() {
        return snappedPoints;
    }

    public void setSnappedPointsLiveData(List<LatLng> snappedPoints) {
       this.snappedPoints.setValue(snappedPoints);
    }

    public MutableLiveData<List<List<LatLng>>> getDirectionsPoints() {
        return directionsPoints;
    }

    public void setDirectionsPoints(List<List<LatLng>> directionsPoints) {
        this.directionsPoints.setValue(directionsPoints);
    }
}
