package com.project.ecoact.data.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface MistralApi {

    @POST("v1/chat/completions")
    Call<MistralResponse> getAdvice(
            @Header("Authorization") String authorization,
            @Body MistralRequest body
    );
}
