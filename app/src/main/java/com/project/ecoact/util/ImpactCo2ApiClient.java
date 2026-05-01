package com.project.ecoact.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ImpactCo2ApiClient {
    private static final String BASE_URL = "https://impactco2.fr/api/v1/thematiques/ecv/";

    public CarbonReferenceData fetchCarbonReferences() {
        CarbonReferenceData data = new CarbonReferenceData();
        fetchCategory("electromenager", data);
        fetchCategory("numerique", data);

        if (data.getFootprintsBySlug().isEmpty()) {
            data.setApiAvailable(false);
            data.setMessage("API Impact CO2 indisponible, calcul avec facteurs locaux.");
        } else {
            data.setApiAvailable(true);
            if (data.getMessage().isEmpty()) {
                data.setMessage("Données Impact CO2 chargées.");
            }
        }

        return data;
    }

    private void fetchCategory(String category, CarbonReferenceData data) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + category);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return;
            }

            String response = readStream(connection.getInputStream());
            JSONObject json = new JSONObject(response);
            if (json.has("warning")) {
                data.setMessage("Données Impact CO2 chargées en accès anonyme.");
            }
            JSONArray items = json.getJSONArray("data");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                data.addFootprint(
                        item.getString("slug"),
                        item.getDouble("ecv")
                );
            }
        } catch (Exception ignored) {
            data.setApiAvailable(false);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(InputStream inputStream) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    public static class CarbonReferenceData {
        private final Map<String, Double> footprintsBySlug = new HashMap<>();
        private boolean apiAvailable;
        private String message = "";

        public void addFootprint(String slug, double footprintKgCo2e) {
            footprintsBySlug.put(slug, footprintKgCo2e);
        }

        public double getFootprint(String slug, double fallbackKgCo2e) {
            Double value = footprintsBySlug.get(slug);
            return value == null ? fallbackKgCo2e : value;
        }

        public Map<String, Double> getFootprintsBySlug() {
            return footprintsBySlug;
        }

        public boolean isApiAvailable() {
            return apiAvailable;
        }

        public void setApiAvailable(boolean apiAvailable) {
            this.apiAvailable = apiAvailable;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
