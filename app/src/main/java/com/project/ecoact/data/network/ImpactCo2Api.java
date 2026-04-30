package com.project.ecoact.data.network;

import com.project.ecoact.domain.model.impactco2.AlimentationCategory;
import com.project.ecoact.domain.model.impactco2.EcvData;
import com.project.ecoact.domain.model.impactco2.EcvItem;
import com.project.ecoact.domain.model.impactco2.FruitLegumeData;
import com.project.ecoact.domain.model.impactco2.ImpactResponse;
import com.project.ecoact.domain.model.impactco2.Thematique;
import com.project.ecoact.domain.model.impactco2.TransportData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ImpactCo2Api {
    @GET("thematiques")
    Call<ImpactResponse<Thematique>> getThematiques();

    @GET("thematiques/ecv/{id}")
    Call<ImpactResponse<EcvData>> getEcv(
            @Path("id") String idOrSlug
    );

    @GET("transport")
    Call<ImpactResponse<TransportData>> getTransport(
            @Query("distance") Double distance,
            @Query("passengers") Integer passengers,
            @Query("ignoreradioactive") Boolean ignoreRadioactive
    );

    @GET("chauffage")
    Call<ImpactResponse<EcvItem>> getChauffage(
            @Query("m2") Double m2
    );

    @GET("alimentation")
    Call<ImpactResponse<AlimentationCategory>> getAlimentation();

    @GET("fruitsetlegumes")
    Call<ImpactResponse<FruitLegumeData>> getFruitsEtLegumes(
            @Query("month") Integer month
    );
}

