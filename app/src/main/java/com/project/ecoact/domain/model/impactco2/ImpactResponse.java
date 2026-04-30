package com.project.ecoact.domain.model.impactco2;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ImpactResponse<T> {
    @SerializedName("data")
    private List<T> data;
    
    @SerializedName("warning")
    private String warning;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}

