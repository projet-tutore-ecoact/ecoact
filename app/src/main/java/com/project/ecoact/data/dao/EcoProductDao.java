// Gère l'accès à la table eco_product
package com.project.ecoact.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.project.ecoact.data.entity.EcoProductEntity;

import java.util.List;

@Dao
public interface EcoProductDao {

    // Retourne le nombre de produits de la base eco_product
    @Query("SELECT COUNT(*) FROM eco_products")
    int countProducts();

    // Insère une liste de produits dans la base de données
    // Si un produit existe déjà, il est remplacé grâce à OnConflictStrategy.REPLACE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EcoProductEntity> products);

    // Récupère tous les produits actifs,
    // triés d'abord par score de priorité décroissant,
    // puis par marque dans l'ordre alphabétique.
    @Query("SELECT * FROM eco_products WHERE active = 1 ORDER BY priority_score DESC, brand ASC")
    List<EcoProductEntity> getActiveProducts();

    // Récupère les produits actifs appartenant à certaines catégories seulement.
    // Les catégories sont données dans la liste categories.
    // Les résultats sont triés par score de priorité décroissant,
    // puis par marque dans l'ordre alphabétique.
    @Query("SELECT * FROM eco_products WHERE active = 1 AND category IN (:categories) ORDER BY priority_score DESC, brand ASC")
    List<EcoProductEntity> getProductsForCategories(List<String> categories);
}
