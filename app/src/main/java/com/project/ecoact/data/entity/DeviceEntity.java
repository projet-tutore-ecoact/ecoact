// à quoi ressemble la table device ?
// Quels attributs et quel est leur type ?
package com.project.ecoact.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(
        // Nom de la table et sa foreign key vers la table utilisateur (chaque device appartient à qlqn)
        tableName = "devices",
        foreignKeys = @ForeignKey(
                // Table parent
                entity = User.class,
                // La colonne de référence de la table parent (user)
                parentColumns = "id",
                // Colonne de la table de device qui contiendra l'id récupéré
                childColumns = "user_id",
                // Si l'utilisateur est supprimé tous ses appareils sont supprimés
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("user_id")}
)
public class DeviceEntity {
    // Clé primaire de la table devices
    // autoGenerate = true signifie que Room génère automatiquement l'id
    @PrimaryKey(autoGenerate = true)
    private long id;

    // Colonne user_id : identifiant de l'utilisateur propriétaire de l'appareil
    @ColumnInfo(name = "user_id")
    private long userId;

    // Colonne type : type d'appareil, par exemple "frigo", "ordinateur", "lampe"
    @ColumnInfo(name = "type")
    private String type;

    // Colonne reference : nom, modèle ou référence de l'appareil
    @ColumnInfo(name = "reference")
    private String reference;

    // Colonne daily_usage_hours : durée d'utilisation quotidienne en heures
    @ColumnInfo(name = "daily_usage_hours")
    private double dailyUsageHours;

    // Colonne daily_consumption_kwh : consommation quotidienne en kWh
    @ColumnInfo(name = "daily_consumption_kwh")
    private double dailyConsumptionKwh;

    // Colonne created_at : date de création de l'appareil,
    // stockée sous forme de timestamp en millisecondes
    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Colonne updated_at : date de dernière modification,
    // stockée sous forme de timestamp en millisecondes
    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Constructeur pour créer un nouvel appareil dans le code.
    // @Ignore indique à Room de ne pas utiliser ce constructeur pour reconstruire l'objet depuis la base.
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

    // Constructeur vide obligatoire pour Room
    // Room l'utilise pour recréer un objet DeviceEntity depuis une ligne de la base
    public DeviceEntity() {
    }

    // Guetters et setters pour la classe
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
