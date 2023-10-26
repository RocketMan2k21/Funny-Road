package com.bestdeveloper.funnyroad.db;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.bestdeveloper.funnyroad.BuildConfig;
import com.bestdeveloper.funnyroad.api.RetrofitService;
import com.bestdeveloper.funnyroad.model.Location;
import com.bestdeveloper.funnyroad.model.SnappedPoint;
import com.bestdeveloper.funnyroad.model.SnappedPointResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Repository {

    private List<LatLng>  snappedPoints    = new ArrayList<>();


    public List<LatLng> getSnappedPoints(String path){
        RetrofitService.SnappingPointsService snappingPointsService = RetrofitService.getSnappingPointsService();

        Call<SnappedPointResult> call = snappingPointsService.getSnappedPoints(true, path, BuildConfig.MAPS_API_KEY);

        call.enqueue(new Callback<SnappedPointResult>() {
            @Override
            public void onResponse(@NonNull Call<SnappedPointResult> call, @NonNull Response<SnappedPointResult> response) {
                SnappedPointResult result = response.body();
                if(result != null && result.getSnappedPoints() != null ){
                    Log.i("REP", "Response successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SnappedPointResult> call, @NonNull Throwable t) {
                Log.e("REP", "Retrofit: failure response" );
            }
        });
        return snappedPoints;
    }



}
