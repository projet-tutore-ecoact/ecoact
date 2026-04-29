package com.project.ecoact.domain.model.impactco2;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AlimentationCategory {
    @SerializedName("name")
    private String name;

    @SerializedName("slug")
    private String slug;

    @SerializedName("items")
    private List<EcvItem> items;

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

    public List<EcvItem> getItems() {
        return items;
    }

    public void setItems(List<EcvItem> items) {
        this.items = items;
    }
}

