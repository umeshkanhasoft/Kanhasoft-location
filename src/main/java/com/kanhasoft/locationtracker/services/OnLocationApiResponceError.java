package com.kanhasoft.locationtracker.services;

import com.kanhasoft.locationtracker.retro.responce.LocationResponce;

public interface OnLocationApiResponceError {
    public void onLocationSucess(LocationResponce locationResponce);
    public void onError(String error);
}
