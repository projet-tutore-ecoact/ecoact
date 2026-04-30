package com.project.ecoact.domain.model.impactco2;

import com.google.gson.annotations.SerializedName;

public class EcvItem {
    @SerializedName("name")
    private String name;

    @SerializedName("ecv")
    private Double ecv;

    @SerializedName("slug")
    private String slug;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getEcv() {
        return ecv;
    }

    public void setEcv(Double ecv) {
        this.ecv = ecv;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}

