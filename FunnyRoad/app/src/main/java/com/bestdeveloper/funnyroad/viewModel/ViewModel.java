package com.bestdeveloper.funnyroad.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bestdeveloper.funnyroad.db.Repository;
import com.bestdeveloper.funnyroad.model.SnappedPoint;

import java.util.List;

public class ViewModel extends AndroidViewModel {

    private MutableLiveData snappedPoints = new MutableLiveData();


    public ViewModel(Application application){
        super(application);


    }

}
