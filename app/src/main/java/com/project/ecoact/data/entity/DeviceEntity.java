package com.project.ecoact.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "devices",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("user_id")}
)
public class DeviceEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "reference")
    private String reference;

    @ColumnInfo(name = "daily_usage_hours")
    private double dailyUsageHours;

    @ColumnInfo(name = "daily_consumption_kwh")
    private double dailyConsumptionKwh;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    @Ignore
    public DeviceEntity(long userId, String type, String reference, double dailyUsageHours, double dailyConsumptionKwh) {
        this.userId = userId;
        this.type = type;
        this.reference = reference;
        this.dailyUsageHours = dailyUsageHours;
        this.dailyConsumptionKwh = dailyConsumptionKwh;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public DeviceEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public double getDailyUsageHours() {
        return dailyUsageHours;
    }

    public void setDailyUsageHours(double dailyUsageHours) {
        this.dailyUsageHours = dailyUsageHours;
    }

    public double getDailyConsumptionKwh() {
        return dailyConsumptionKwh;
    }

    public void setDailyConsumptionKwh(double dailyConsumptionKwh) {
        this.dailyConsumptionKwh = dailyConsumptionKwh;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
