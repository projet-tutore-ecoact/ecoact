package com.project.ecoact.domain.model.impactco2;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EcvData {
    @SerializedName("name")
    private String name;

    @SerializedName("slug")
    private String slug;

    @SerializedName("ecv")
    private Double ecv;

    @SerializedName("footprint")
    private Double footprint;

    @SerializedName("endOfLife")
    private Double endOfLife;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Double getEcv() {
        return ecv;
    }

    public void setEcv(Double ecv) {
        this.ecv = ecv;
    }

    public Double getFootprint() {
        return footprint;
    }

    public void setFootprint(Double footprint) {
        this.footprint = footprint;
    }

    public Double getEndOfLife() {
        return endOfLife;
    }

    public void setEndOfLife(Double endOfLife) {
        this.endOfLife = endOfLife;
    }
}

