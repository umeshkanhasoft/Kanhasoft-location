
package com.kanhasoft.locationtracker.retro.responce;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationResponceData {

    @SerializedName("lat")
    @Expose
    private Double latatitude;

    @SerializedName("long")
    @Expose
    private Double longitude;

    @SerializedName("updateloc")
    @Expose
    private Boolean updateloc;

    @SerializedName("userid")
    @Expose
    private Long userid;

    public Double getLat() {
        return latatitude;
    }

    public void setLat(Double latatitude) {
        this.latatitude = latatitude;
    }

    public Double getLong() {
        return longitude;
    }

    public void setLong(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getUpdateloc() {
        return updateloc;
    }

    public void setUpdateloc(Boolean updateloc) {
        this.updateloc = updateloc;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

}
