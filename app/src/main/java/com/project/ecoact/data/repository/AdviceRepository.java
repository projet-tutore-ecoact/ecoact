package com.project.ecoact.data.repository;

import com.project.ecoact.data.network.MistralApi;
import com.project.ecoact.data.network.MistralRequest;
import com.project.ecoact.data.network.MistralResponse;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdviceRepository {

    private static final String BASE_URL = "https://api.mistral.ai/";
    private static final String API_KEY = "Bearer JbZbrQX46mrGVU9IVu5oGCw7NTnV4sPj";
    private static final String MODEL = "mistral-small-latest";

    private final MistralApi api;

    public AdviceRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(MistralApi.class);
    }

    public void getAdvice(String habitsData, String devicesData, AdviceCallback callback) {
        String prompt = "Tu es un assistant éco-responsable. " +
                "Voici les habitudes de l'utilisateur : " + habitsData + ". " +
                "Voici ses appareils : " + devicesData + ". " +
                "Donne 3 conseils personnalisés courts et pratiques en français pour réduire sa consommation d'énergie.";

        List<MistralRequest.Message> messages = Arrays.asList(
                new MistralRequest.Message("user", prompt)
        );

        MistralRequest request = new MistralRequest(MODEL, messages);

        api.getAdvice(API_KEY, request).enqueue(new Callback<MistralResponse>() {
            @Override
            public void onResponse(Call<MistralResponse> call, Response<MistralResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MistralResponse.Choice> choices = response.body().getChoices();
                    if (choices != null && !choices.isEmpty()) {
                        callback.onSuccess(choices.get(0).getMessage().getContent());
                    } else {
                        callback.onError("Aucun conseil reçu");
                    }
                } else {
                    callback.onError("Erreur API : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MistralResponse> call, Throwable t) {
                callback.onError("Erreur réseau : " + t.getMessage());
            }
        });
    }

    public interface AdviceCallback {
        void onSuccess(String advice);
        void onError(String error);
    }
}
