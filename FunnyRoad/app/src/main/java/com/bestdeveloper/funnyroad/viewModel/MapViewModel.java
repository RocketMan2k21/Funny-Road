package com.bestdeveloper.funnyroad.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.bestdeveloper.funnyroad.db.Repository;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapViewModel extends AndroidViewModel {
    private Repository repository;
    private MutableLiveData<PolylineOptions> optionsMutableLiveData = new MutableLiveData<>();

    public MapViewModel(Application application){
        super(application);

        repository = new Repository();
    }

    public void setOptionsMutableLiveData(PolylineOptions polylineOptions) {
        this.optionsMutableLiveData.setValue(polylineOptions);
    }

    public MutableLiveData<PolylineOptions> getOptionsMutableLiveData() {
        return optionsMutableLiveData;
    }
}
