package com.kanhasoft.locationtracker.retro;


public interface IResult {
    public void notifySuccessAsObject(String requestType, Object response);

    public void onError(String message);
}
