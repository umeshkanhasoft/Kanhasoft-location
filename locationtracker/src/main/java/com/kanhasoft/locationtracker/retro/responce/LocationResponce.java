
package com.kanhasoft.locationtracker.retro.responce;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationResponce {

    @SerializedName("code")
    @Expose
    private Long code;

    @SerializedName("data")
    @Expose
    private LocationResponceData data;

    @SerializedName("error")
    @Expose
    private Boolean error;

    @SerializedName("message")
    @Expose
    private String message;

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public LocationResponceData getData() {
        return data;
    }

    public void setData(LocationResponceData data) {
        this.data = data;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
