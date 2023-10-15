package com.bestdeveloper.funnyroad.api;

import com.bestdeveloper.funnyroad.model.SnappedPointResult;
import com.bestdeveloper.funnyroad.service.Utils;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RetrofitService {

    static SnappingPointsService snappingPointsService = null;

    public static SnappingPointsService getSnappingPointsService(){
        if(snappingPointsService == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Utils.SNAPPING_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            snappingPointsService = retrofit.create(SnappingPointsService.class);
        }
        return snappingPointsService;
    }


    public interface SnappingPointsService {
        @GET("snapToRoads")
        Call<SnappedPointResult> getSnappedPoints(@Query("interpolate") boolean interpolation,
                                                  @Query("path") String location,
                                                  @Query("key") String apiKey );
    }
}




