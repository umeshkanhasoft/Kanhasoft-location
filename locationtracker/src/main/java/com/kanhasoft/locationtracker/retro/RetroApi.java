package com.kanhasoft.locationtracker.retro;

import com.kanhasoft.locationtracker.retro.request.LocationApiRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetroApi {

    @POST("/game/updateloc")
    @Headers({"Content-Type: application/json"})
    Call<ResponseBody> callLocationApi(@Body LocationApiRequest locationRequest, @Header("authorization") String authorization);
}
