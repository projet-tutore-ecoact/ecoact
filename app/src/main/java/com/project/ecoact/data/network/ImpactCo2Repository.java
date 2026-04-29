package com.project.ecoact.data.network;

import com.project.ecoact.domain.model.impactco2.AlimentationCategory;
import com.project.ecoact.domain.model.impactco2.EcvData;
import com.project.ecoact.domain.model.impactco2.EcvItem;
import com.project.ecoact.domain.model.impactco2.FruitLegumeData;
import com.project.ecoact.domain.model.impactco2.ImpactResponse;
import com.project.ecoact.domain.model.impactco2.Thematique;
import com.project.ecoact.domain.model.impactco2.TransportData;

import retrofit2.Call;

public class ImpactCo2Repository {

    private final ImpactCo2Api api;

    public ImpactCo2Repository() {
        this.api = RetrofitClient.getClient().create(ImpactCo2Api.class);
    }

    public Call<ImpactResponse<Thematique>> getThematiques() {
        return api.getThematiques();
    }

    public Call<ImpactResponse<EcvData>> getEcv(String idOrSlug) {
        return api.getEcv(idOrSlug);
    }

    public Call<ImpactResponse<TransportData>> getTransport(Double distance, Integer passengers, Boolean ignoreRadioactive) {
        return api.getTransport(distance, passengers, ignoreRadioactive);
    }
    
    public Call<ImpactResponse<EcvItem>> getChauffage(Double m2) {
        return api.getChauffage(m2);
    }

    public Call<ImpactResponse<AlimentationCategory>> getAlimentation() {
        return api.getAlimentation();
    }

    public Call<ImpactResponse<FruitLegumeData>> getFruitsEtLegumes(Integer month) {
        return api.getFruitsEtLegumes(month);
    }
}

