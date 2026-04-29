package com.project.ecoact.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.project.ecoact.R;
import com.project.ecoact.data.network.ImpactCo2Repository;
import com.project.ecoact.domain.model.impactco2.AlimentationCategory;
import com.project.ecoact.domain.model.impactco2.EcvData;
import com.project.ecoact.domain.model.impactco2.EcvItem;
import com.project.ecoact.domain.model.impactco2.ImpactResponse;
import com.project.ecoact.domain.model.impactco2.TransportData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImpactDemoFragment extends Fragment {

    private ImpactCo2Repository repository;
    private TextView tvResult;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_impact_demo, container, false);

        repository = new ImpactCo2Repository();

        tvResult = view.findViewById(R.id.tvResult);
        progressBar = view.findViewById(R.id.progressBar);

        Button btnLoadTransport = view.findViewById(R.id.btnLoadTransport);
        Button btnLoadEcv = view.findViewById(R.id.btnLoadEcv);
        Button btnLoadAlimentation = view.findViewById(R.id.btnLoadAlimentation);

        btnLoadTransport.setOnClickListener(v -> loadTransport());
        btnLoadEcv.setOnClickListener(v -> loadEcvNumerique());
        btnLoadAlimentation.setOnClickListener(v -> loadAlimentation());

        return view;
    }

    private void loadTransport() {
        showLoading();
        // 10 km, 1 passager
        repository.getTransport(50.0, 1, false).enqueue(new Callback<ImpactResponse<TransportData>>() {
            @Override
            public void onResponse(Call<ImpactResponse<TransportData>> call, Response<ImpactResponse<TransportData>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    StringBuilder sb = new StringBuilder("Impact pour 50 km de transport :\n\n");
                    for (TransportData data : response.body().getData()) {
                        sb.append("- ").append(data.getName()).append(" : ").append(data.getValue()).append(" kg CO2e\n");
                    }
                    tvResult.setText(sb.toString());
                } else {
                    tvResult.setText("Erreur : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ImpactResponse<TransportData>> call, Throwable t) {
                hideLoading();
                tvResult.setText("Erreur réseau : " + t.getMessage());
            }
        });
    }

    private void loadEcvNumerique() {
        showLoading();
        repository.getEcv("numerique").enqueue(new Callback<ImpactResponse<EcvData>>() {
            @Override
            public void onResponse(Call<ImpactResponse<EcvData>> call, Response<ImpactResponse<EcvData>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    StringBuilder sb = new StringBuilder("ECV de la catégorie Numérique :\n\n");
                    for (EcvData data : response.body().getData()) {
                        sb.append("- ").append(data.getName()).append(" : ").append(data.getEcv()).append(" kg CO2e\n");
                    }
                    tvResult.setText(sb.toString());
                } else {
                    tvResult.setText("Erreur : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ImpactResponse<EcvData>> call, Throwable t) {
                hideLoading();
                tvResult.setText("Erreur réseau : " + t.getMessage());
            }
        });
    }

    private void loadAlimentation() {
        showLoading();
        repository.getAlimentation().enqueue(new Callback<ImpactResponse<AlimentationCategory>>() {
            @Override
            public void onResponse(Call<ImpactResponse<AlimentationCategory>> call, Response<ImpactResponse<AlimentationCategory>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    StringBuilder sb = new StringBuilder("Alimentation :\n\n");
                    for (AlimentationCategory category : response.body().getData()) {
                        sb.append("[").append(category.getName()).append("]\n");
                        if (category.getItems() != null) {
                            for (EcvItem item : category.getItems()) {
                                sb.append("  - ").append(item.getName()).append(" : ").append(item.getEcv()).append(" kg CO2e\n");
                            }
                        }
                    }
                    tvResult.setText(sb.toString());
                } else {
                    tvResult.setText("Erreur : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ImpactResponse<AlimentationCategory>> call, Throwable t) {
                hideLoading();
                tvResult.setText("Erreur réseau : " + t.getMessage());
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        tvResult.setText("Chargement en cours...");
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }
}

