// Attributs et types et setters guetters pour la table EcoProduct
package com.project.ecoact.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// à quoi ressemble la table
@Entity(
        tableName = "eco_products",
        indices = {
                @Index("category"),
                @Index(value = {"category", "reference"}, unique = true)
        }
)
public class EcoProductEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "brand")
    private String brand;

    @ColumnInfo(name = "reference")
    private String reference;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "energy_class")
    private String energyClass;

    @ColumnInfo(name = "energy_info")
    private String energyInfo;

    @ColumnInfo(name = "repairability_score")
    private String repairabilityScore;

    @ColumnInfo(name = "price_label")
    private String priceLabel;

    @ColumnInfo(name = "purchase_url")
    private String purchaseUrl;

    @ColumnInfo(name = "reason")
    private String reason;

    @ColumnInfo(name = "priority_score")
    private int priorityScore;

    @ColumnInfo(name = "active")
    private boolean active;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public EcoProductEntity() {
    }

    // Constructeur de la table
    @Ignore
    public EcoProductEntity(String category, String brand, String reference, String name,
                            String energyClass, String energyInfo, String repairabilityScore,
                            String priceLabel, String purchaseUrl, String reason,
                            int priorityScore) {
        this.category = category;
        this.brand = brand;
        this.reference = reference;
        this.name = name;
        this.energyClass = energyClass;
        this.energyInfo = energyInfo;
        this.repairabilityScore = repairabilityScore;
        this.priceLabel = priceLabel;
        this.purchaseUrl = purchaseUrl;
        this.reason = reason;
        this.priorityScore = priorityScore;
        this.active = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Guetters et setters pour la classe
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnergyClass() {
        return energyClass;
    }

    public void setEnergyClass(String energyClass) {
        this.energyClass = energyClass;
    }

    public String getEnergyInfo() {
        return energyInfo;
    }

    public void setEnergyInfo(String energyInfo) {
        this.energyInfo = energyInfo;
    }

    public String getRepairabilityScore() {
        return repairabilityScore;
    }

    public void setRepairabilityScore(String repairabilityScore) {
        this.repairabilityScore = repairabilityScore;
    }

    public String getPriceLabel() {
        return priceLabel;
    }

    public void setPriceLabel(String priceLabel) {
        this.priceLabel = priceLabel;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public void setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(int priorityScore) {
        this.priorityScore = priorityScore;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
