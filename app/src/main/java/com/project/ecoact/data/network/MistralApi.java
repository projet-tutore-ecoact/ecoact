package com.project.ecoact.data.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
/**
 *On utilise l'interface retrofit pour L'API Mistral.
 * Retrofit génère automatiquement le code HTTP à partir de ces annotations.
 */
public interface MistralApi {

    @POST("v1/chat/completions")
    Call<MistralResponse> getAdvice(
            @Header("Authorization") String authorization,
            @Body MistralRequest body
    );
}
