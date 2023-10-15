package com.bestdeveloper.funnyroad.db;

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

    private List<LatLng> snappedPoints = new ArrayList<>();

    public List<LatLng> getSnappedPoints(String path){
        RetrofitService.SnappingPointsService snappingPointsService = RetrofitService.getSnappingPointsService();

        Call<SnappedPointResult> call = snappingPointsService.getSnappedPoints(true, path, BuildConfig.MAPS_API_KEY);

        call.enqueue(new Callback<SnappedPointResult>() {
            @Override
            public void onResponse(Call<SnappedPointResult> call, Response<SnappedPointResult> response) {
                SnappedPointResult result = response.body();
                if(result != null && result.getSnappedPoints() != null ){
                    getPoints(result.getSnappedPoints());
                }
            }

            @Override
            public void onFailure(Call<SnappedPointResult> call, Throwable t) {

            }
        });

        return snappedPoints;
    }

    private void getPoints(List<SnappedPoint> pointsToSnap){
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < pointsToSnap.size(); i++){
                    Location location = pointsToSnap.get(i).getLocation();
                    snappedPoints.add(new LatLng(location.getLongitude(),
                            location.getLatitude()));
                }
            }
        });

    }


}
