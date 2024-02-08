package com.bestdeveloper.funnyroad.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.bestdeveloper.funnyroad.db.Repository;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapViewModel extends AndroidViewModel {
    private Repository repository;
    private MutableLiveData<List<LatLng>> pointsMutableLiveData = new MutableLiveData<>();

    public MapViewModel(Application application){
        super(application);

        repository = new Repository();
    }

    public MutableLiveData<List<LatLng>> getPointsMutableLiveData() {
        return pointsMutableLiveData;
    }

    public void setSnappedPointsLiveData(List<LatLng> snappedPoints) {
       pointsMutableLiveData.setValue(snappedPoints);
    }
}
