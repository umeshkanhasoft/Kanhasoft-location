package com.kanhasoft.locationtracker.retro.apicall;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kanhasoft.locationtracker.retro.IResult;
import com.kanhasoft.locationtracker.retro.RetroApi;
import com.kanhasoft.locationtracker.retro.ServiceGenerator;
import com.kanhasoft.locationtracker.retro.request.LocationApiRequest;
import com.kanhasoft.locationtracker.retro.responce.LocationResponce;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiKanhasoftLocation {

    private RetroApi retroApi;
    private IResult responseInterface;
    private Context mContext;

    public ApiKanhasoftLocation(Context mContext, IResult responseInterface) {
        retroApi = ServiceGenerator.createService(RetroApi.class);
        this.responseInterface = responseInterface;
        this.mContext = mContext;
    }

    public void execute(LocationApiRequest locationApiRequest, String headerParam) {
        if (isNetworkAvailable(mContext)) {
            final Gson gson = new GsonBuilder().create();
            Call<ResponseBody> responseBodyCall = retroApi.callLocationApi(locationApiRequest, headerParam);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.code() == 200) {
                            JSONObject masterDetails = new JSONObject(response.body().string());
                            try {
                                responseInterface.notifySuccessAsObject("WorkOrder", gson.fromJson(masterDetails.toString(), LocationResponce.class));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            String res = response.errorBody().string();
                            responseInterface.onError(res);
                        }

                    } catch (Exception e) {
                        responseInterface.onError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    responseInterface.onError(t.getMessage());
                }
            });
        } else {
            responseInterface.onError("Please check your internet connection");
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
