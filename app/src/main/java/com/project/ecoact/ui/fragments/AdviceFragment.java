package com.project.ecoact.ui.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.project.ecoact.R;
import com.project.ecoact.data.entity.DeviceEntity;
import com.project.ecoact.data.entity.EcoProductEntity;
import com.project.ecoact.data.entity.HabitEntity;
import com.project.ecoact.data.repository.AdviceRepository;
import com.project.ecoact.data.repository.DeviceRepository;
import com.project.ecoact.data.repository.EcoProductRepository;
import com.project.ecoact.data.repository.HabitRepository;
import com.project.ecoact.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdviceFragment extends Fragment {

    private TextView tvAdvice, tvError, tvMarketplaceStatus;
    private Button btnGenerate, btnSuggestDevices;
    private ProgressBar progressBar, marketplaceProgress;
    private LinearLayout adviceCardsContainer, marketplaceProductsContainer;
    private boolean suggestionsRequested = false;

    private AdviceRepository adviceRepository;
    private HabitRepository habitRepository;
    private DeviceRepository deviceRepository;
    private EcoProductRepository ecoProductRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_advice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAdvice = view.findViewById(R.id.tv_advice);
        tvError = view.findViewById(R.id.tv_advice_error);
        tvMarketplaceStatus = view.findViewById(R.id.tv_marketplace_status);
        btnGenerate = view.findViewById(R.id.btn_generate_advice);
        btnSuggestDevices = view.findViewById(R.id.btn_suggest_devices);
        progressBar = view.findViewById(R.id.progress_advice);
        marketplaceProgress = view.findViewById(R.id.progress_marketplace);
        adviceCardsContainer = view.findViewById(R.id.layout_advice_cards);
        marketplaceProductsContainer = view.findViewById(R.id.layout_marketplace_products);

        adviceRepository = new AdviceRepository();
        habitRepository = new HabitRepository(requireActivity().getApplication());
        deviceRepository = new DeviceRepository(requireActivity().getApplication());
        ecoProductRepository = new EcoProductRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        btnGenerate.setOnClickListener(v -> generateAdvice());
        btnSuggestDevices.setOnClickListener(v -> {
            suggestionsRequested = true;
            loadMarketplaceRecommendations();
        });

        showMarketplaceIdleState();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (suggestionsRequested) {
            loadMarketplaceRecommendations();
        } else if (sessionManager != null) {
            showMarketplaceIdleState();
        }
    }
// Lance la génération de conseils via Mistral en back 
    private void generateAdvice() {
        showLoading(true);
        tvError.setVisibility(View.GONE);
        adviceCardsContainer.removeAllViews();
        tvAdvice.setVisibility(View.VISIBLE);
        tvAdvice.setText("Génération des conseils en cours...");

        Long userId = sessionManager.getCurrentUserId();
        if (userId == null) {
            showLoading(false);
            tvError.setText("Connectez-vous pour obtenir des conseils.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        new Thread(() -> {
            String habitsData = habitRepository.getHabitsSummary(userId);
            String devicesData = deviceRepository.getDevicesSummary(userId);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adviceRepository.getAdvice(habitsData, devicesData, new AdviceRepository.AdviceCallback() {
                    @Override
                    public void onSuccess(String advice) {
                        showLoading(false);
                        renderAdviceCards(advice);
                        if (suggestionsRequested) {
                            loadMarketplaceRecommendations();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        tvError.setText("Erreur : " + error);
                        tvError.setVisibility(View.VISIBLE);
                        if (suggestionsRequested) {
                            loadMarketplaceRecommendations();
                        }
                    }
                }));
            }
        }).start();
    }

    private void showMarketplaceIdleState() {
        Long userId = sessionManager.getCurrentUserId();
        marketplaceProductsContainer.removeAllViews();
        setMarketplaceLoading(false);

        if (userId == null) {
            btnSuggestDevices.setEnabled(false);
            tvMarketplaceStatus.setText("Connectez-vous pour voir les appareils suggérés.");
            return;
        }

        btnSuggestDevices.setEnabled(true);
        tvMarketplaceStatus.setText("Cliquez pour analyser votre profil et afficher des appareils adaptés.");
    }
// Charge les suggestions d'appareils eco-responsables selon le profil
    private void loadMarketplaceRecommendations() {
        Long userId = sessionManager.getCurrentUserId();
        marketplaceProductsContainer.removeAllViews();

        if (userId == null) {
            setMarketplaceLoading(false);
            tvMarketplaceStatus.setText("Connectez-vous pour voir les appareils suggérés.");
            return;
        }

        setMarketplaceLoading(true);
        tvMarketplaceStatus.setText("Analyse du profil en cours...");

        new Thread(() -> {
            List<DeviceEntity> devices = deviceRepository.getDevicesByUserSync(userId);
            List<HabitEntity> habits = habitRepository.getHabitsByUserSync(userId);
            EcoProductRepository.RecommendationResult result =
                    ecoProductRepository.getRecommendations(devices, habits);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> renderMarketplace(result));
            }
        }).start();
    }

    private void renderMarketplace(EcoProductRepository.RecommendationResult result) {
        setMarketplaceLoading(false);
        marketplaceProductsContainer.removeAllViews();
        tvMarketplaceStatus.setText(result.getMessage());

        if (result.isTopProfile() || result.getProducts().isEmpty()) {
            return;
        }

        for (EcoProductEntity product : result.getProducts()) {
            marketplaceProductsContainer.addView(createProductCard(product));
        }
    }
// Découpe la réponse Mistral en cartes individuels 
    private void renderAdviceCards(String advice) {
        adviceCardsContainer.removeAllViews();
        List<String> adviceItems = extractAdviceItems(advice);

        if (adviceItems.isEmpty()) {
            tvAdvice.setVisibility(View.VISIBLE);
            tvAdvice.setText(advice);
            return;
        }

        tvAdvice.setVisibility(View.GONE);
        for (String item : adviceItems) {
            adviceCardsContainer.addView(createAdviceCard(item));
        }
    }

    private View createAdviceCard(String adviceText) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(createAdviceCardBackground());

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardParams);

        ImageView bulbIcon = new ImageView(requireContext());
        bulbIcon.setImageResource(R.drawable.ic_lightbulb);
        bulbIcon.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.primary_orange)
        ));
        bulbIcon.setBackground(createAdviceIconBackground());
        bulbIcon.setPadding(dp(8), dp(8), dp(8), dp(8));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        iconParams.setMargins(0, 0, dp(12), 0);
        card.addView(bulbIcon, iconParams);

        TextView text = new TextView(requireContext());
        text.setText(adviceText);
        text.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        text.setTextSize(14);
        text.setLineSpacing(dp(3), 1f);
        card.addView(text, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        return card;
    }

    private View createProductCard(EcoProductEntity product) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(createCardBackground());
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(10));

        ImageView image = new ImageView(requireContext());
        image.setImageResource(R.drawable.ic_devices);
        image.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.accent_teal)
        ));
        image.setBackground(createProductImageBackground());
        image.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(64), dp(64));
        imageParams.setMargins(0, 0, dp(12), 0);
        header.addView(image, imageParams);

        LinearLayout titleColumn = new LinearLayout(requireContext());
        titleColumn.setOrientation(LinearLayout.VERTICAL);

        TextView reference = new TextView(requireContext());
        reference.setText(safe(product.getBrand()) + " " + safe(product.getReference()));
        reference.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_orange));
        reference.setTextSize(13);
        reference.setTypeface(null, Typeface.BOLD);
        titleColumn.addView(reference);

        TextView name = new TextView(requireContext());
        name.setText(safe(product.getName()));
        name.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        name.setTextSize(17);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, dp(4), 0, 0);
        titleColumn.addView(name);

        header.addView(titleColumn, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        card.addView(header);

        TextView details = new TextView(requireContext());
        details.setText("Classe : " + safe(product.getEnergyClass()) +
                "\nConso : " + safe(product.getEnergyInfo()) +
                "\nRéparabilité : " + safe(product.getRepairabilityScore()));
        details.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
        details.setTextSize(14);
        details.setLineSpacing(dp(2), 1f);
        card.addView(details);

        TextView reason = new TextView(requireContext());
        reason.setText(safe(product.getReason()));
        reason.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        reason.setTextSize(14);
        reason.setPadding(0, dp(10), 0, dp(12));
        card.addView(reason);

        Button buyButton = new Button(requireContext());
        buyButton.setText("Acheter");
        buyButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        buyButton.setTextSize(14);
        buyButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.accent_teal)
        ));
        buyButton.setOnClickListener(v -> openProductLink(product.getPurchaseUrl()));
        card.addView(buyButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        return card;
    }

    private GradientDrawable createCardBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(requireContext(), R.color.white));
        drawable.setCornerRadius(dp(8));
        drawable.setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.bg_light_green));
        return drawable;
    }

    private GradientDrawable createAdviceCardBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(requireContext(), R.color.white));
        drawable.setCornerRadius(dp(8));
        drawable.setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.bg_light_orange));
        return drawable;
    }

    private GradientDrawable createAdviceIconBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(requireContext(), R.color.bg_light_orange));
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable createProductImageBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(requireContext(), R.color.bg_light_green));
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private void openProductLink(String url) {
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Lien indisponible", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Impossible d'ouvrir le lien", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!loading);
    }

    private void setMarketplaceLoading(boolean loading) {
        marketplaceProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSuggestDevices.setEnabled(!loading && sessionManager.getCurrentUserId() != null);
    }
// Parse le texte brut de Mistral pour extraire une liste de conseils
    private List<String> extractAdviceItems(String advice) {
        List<String> items = new ArrayList<>();
        if (advice == null || advice.trim().isEmpty()) {
            return items;
        }

        StringBuilder currentItem = new StringBuilder();
        String[] lines = advice.split("\\r?\\n");
        for (String line : lines) {
            String cleanedLine = cleanAdviceLine(line);
            if (cleanedLine.isEmpty() || isAdviceHeading(cleanedLine)) {
                continue;
            }

            String lowerLine = cleanedLine.toLowerCase(Locale.FRANCE);
            boolean startsNewItem = line.trim().matches("^([-*\\u2022]|\\d+[\\).]|[A-Za-z][\\).])\\s+.*")
                    || lowerLine.matches("^conseil\\s+\\d+.*")
                    || lowerLine.matches("^priorit[eé]\\s+\\d+.*");

            if (startsNewItem && currentItem.length() > 0) {
                items.add(currentItem.toString());
                currentItem.setLength(0);
            }

            if (currentItem.length() > 0) {
                currentItem.append(" ");
            }
            currentItem.append(cleanedLine);
        }

        if (currentItem.length() > 0) {
            items.add(currentItem.toString());
        }
        return items;
    }

    private boolean isAdviceHeading(String value) {
        String normalized = value.replace(":", "")
                .trim()
                .toUpperCase(Locale.FRANCE);
        return normalized.equals("CONSEILS")
                || normalized.equals("PRIORITÉS")
                || normalized.equals("PRIORITES")
                || normalized.startsWith("CONSEILS ")
                || normalized.startsWith("PRIORITÉS ")
                || normalized.startsWith("PRIORITES ");
    }

    private String cleanAdviceLine(String line) {
        if (line == null) {
            return "";
        }
        String cleaned = line.trim()
                .replace("**", "")
                .replace("__", "");
        cleaned = cleaned.replaceFirst("^#{1,6}\\s*", "");
        cleaned = cleaned.replaceFirst("^[-*\\u2022]\\s*", "");
        cleaned = cleaned.replaceFirst("^\\d+[\\).]\\s*", "");
        cleaned = cleaned.replaceFirst("^[A-Za-z][\\).]\\s*", "");
        return cleaned.trim();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
