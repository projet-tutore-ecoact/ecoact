package com.project.ecoact.domain.model.impactco2;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FruitLegumeData {
    @SerializedName("name")
    private String name;

    @SerializedName("slug")
    private String slug;

    @SerializedName("ecv")
    private Double ecv;

    @SerializedName("category")
    private String category;

    @SerializedName("months")
    private List<Integer> months;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Integer> getMonths() {
        return months;
    }

    public void setMonths(List<Integer> months) {
        this.months = months;
    }
}

