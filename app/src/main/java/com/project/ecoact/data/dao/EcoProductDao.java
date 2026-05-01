package com.project.ecoact.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.project.ecoact.data.entity.EcoProductEntity;

import java.util.List;

@Dao
public interface EcoProductDao {
    @Query("SELECT COUNT(*) FROM eco_products")
    int countProducts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EcoProductEntity> products);

    @Query("SELECT * FROM eco_products WHERE active = 1 ORDER BY priority_score DESC, brand ASC")
    List<EcoProductEntity> getActiveProducts();

    @Query("SELECT * FROM eco_products WHERE active = 1 AND category IN (:categories) ORDER BY priority_score DESC, brand ASC")
    List<EcoProductEntity> getProductsForCategories(List<String> categories);
}
