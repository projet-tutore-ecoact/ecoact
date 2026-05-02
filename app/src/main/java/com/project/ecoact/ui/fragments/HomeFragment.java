package com.project.ecoact.ui.fragments;

// Imports pour les graphiques et l'interface utilisateur
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

// Bibliothèque MPAndroidChart pour les graphiques
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.project.ecoact.R;
import com.project.ecoact.data.entity.DeviceEntity;
import com.project.ecoact.data.repository.DeviceRepository;
import com.project.ecoact.util.SessionManager;

// Imports pour le formatage des nombres et la gestion des textes
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment principal affichant la consommation électrique
 * Graphiques : évolution (barres) et répartition par appareil (camembert)
 */
public class HomeFragment extends Fragment {

    // ===== CONSTANTES =====
    private static final long DAY_MS = 24L * 60L * 60L * 1000L;  // Nombre de ms dans un jour
    private static final int PERIOD_WEEK_DAYS = 7;   // Période de 7 jours
    private static final int PERIOD_MONTH_DAYS = 30; // Période de 30 jours

    // ===== VUES UI =====
    private TextView tvElectricityValue, tvElectricityTrend, tvPeriodConsumption;
    private TextView tvComparisonSummary, tvDistributionSummary;
    private Button btnWeek, btnMonth;
    private BarChart barChart;   // Graphique à barres pour l'évolution
    private PieChart pieChart;   // Graphique camembert pour la répartition

    // ===== DONNÉES =====
    private DeviceRepository deviceRepository;  // Accès aux données des appareils
    private SessionManager sessionManager;      // Gestion de la session utilisateur
    private int selectedPeriodDays = PERIOD_WEEK_DAYS;  // Période sélectionnée (7 jours par défaut)

    /**
     * Crée la vue du fragment à partir du layout XML
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Appelé après la création de la vue
     * Initialise tous les composants
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisation des dépendances
        deviceRepository = new DeviceRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        // Initialisation de l'interface
        initViews(view);
        setupCharts();
        setupButtons();
        updateButtonStyle(btnWeek, btnMonth);  // Style : bouton 7 jours actif
        loadUserConsumption();                 // Chargement des données
    }

    /**
     * Appelé quand le fragment redevient visible
     * Recharge les données au cas où elles auraient changé
     */
    @Override
    public void onResume() {
        super.onResume();
        if (deviceRepository != null) {
            loadUserConsumption();
        }
    }

    /**
     * Récupère toutes les vues du layout et initialise les conteneurs de graphiques
     */
    private void initViews(View view) {
        // Vues texte
        tvElectricityValue = view.findViewById(R.id.tv_electricity_value);
        tvElectricityTrend = view.findViewById(R.id.tv_electricity_trend);
        tvPeriodConsumption = view.findViewById(R.id.tv_period_consumption);
        tvComparisonSummary = view.findViewById(R.id.tv_comparison_summary);
        tvDistributionSummary = view.findViewById(R.id.tv_distribution_summary);

        // Boutons
        btnWeek = view.findViewById(R.id.btn_week);
        btnMonth = view.findViewById(R.id.btn_month);

        // Création dynamique des graphiques
        barChart = new BarChart(requireContext());
        pieChart = new PieChart(requireContext());

        // Ajout du graphique à barres dans son conteneur
        ViewGroup chartContainer = view.findViewById(R.id.chart_container);
        chartContainer.removeAllViews();
        chartContainer.addView(barChart, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Ajout du camembert dans son conteneur
        ViewGroup pieContainer = view.findViewById(R.id.pie_container);
        pieContainer.removeAllViews();
        pieContainer.addView(pieChart, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    /**
     * Configuration des graphiques (apparence, légendes, axes)
     */
    private void setupCharts() {
        // ===== CONFIGURATION DU GRAPHIQUE À BARRES =====
        barChart.getDescription().setEnabled(false);  // Pas de description
        barChart.setDrawGridBackground(false);        // Pas de grille en arrière-plan
        barChart.setTouchEnabled(false);              // Pas d'interaction tactile
        barChart.setDragEnabled(false);               // Pas de déplacement
        barChart.setScaleEnabled(false);              // Pas de zoom
        barChart.getAxisRight().setEnabled(false);    // Axe droit désactivé
        barChart.getLegend().setEnabled(false);       // Pas de légende
        barChart.setNoDataText("Ajoutez des appareils pour afficher l'évolution.");

        // Configuration de l'axe X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  // Axe en bas
        xAxis.setGranularity(1f);                       // Granularité de 1
        xAxis.setDrawGridLines(false);                  // Pas de lignes de grille

        // ===== CONFIGURATION DU CAMEMBERT =====
        pieChart.getDescription().setEnabled(false);    // Pas de description
        pieChart.setUsePercentValues(true);             // Affichage en pourcentages
        pieChart.setDrawHoleEnabled(true);              // Trou au centre (donut)
        pieChart.setHoleRadius(58f);                    // Rayon du trou
        pieChart.setTransparentCircleRadius(62f);       // Rayon du cercle transparent
        pieChart.setNoDataText("Ajoutez des appareils pour afficher la répartition.");
    }

    /**
     * Configuration des clics sur les boutons 7 jours / 30 jours
     */
    private void setupButtons() {
        // Bouton "7 jours"
        btnWeek.setOnClickListener(v -> {
            selectedPeriodDays = PERIOD_WEEK_DAYS;
            updateButtonStyle(btnWeek, btnMonth);  // Style actif sur btnWeek
            loadUserConsumption();                  // Rechargement des données
        });

        // Bouton "30 jours"
        btnMonth.setOnClickListener(v -> {
            selectedPeriodDays = PERIOD_MONTH_DAYS;
            updateButtonStyle(btnMonth, btnWeek);  // Style actif sur btnMonth
            loadUserConsumption();                  // Rechargement des données
        });
    }

    /**
     * Charge la consommation de l'utilisateur connecté (dans un thread séparé)
     */
    private void loadUserConsumption() {
        Long userId = sessionManager.getCurrentUserId();  // Récupération de l'ID utilisateur
        if (userId == null) {
            showEmptyState("Connectez-vous pour afficher votre consommation.");
            return;
        }

        // Requête en arrière-plan pour ne pas bloquer l'UI
        new Thread(() -> {
            List<DeviceEntity> devices = deviceRepository.getDevicesByUserSync(userId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> renderConsumption(devices));
            }
        }).start();
    }

    /**
     * Affiche les données de consommation dans l'interface
     */
    private void renderConsumption(List<DeviceEntity> devices) {
        List<DeviceEntity> safeDevices = devices == null ? new ArrayList<>() : devices;

        // Calculs
        double dailyKwh = calculateDailyKwh(safeDevices);
        double currentPeriodKwh = dailyKwh * selectedPeriodDays;
        double previousPeriodKwh = calculatePreviousPeriodKwh(safeDevices, selectedPeriodDays);
        double difference = currentPeriodKwh - previousPeriodKwh;

        // Mise à jour des TextViews
        tvElectricityValue.setText(formatNumber(dailyKwh) + " kWh/jour");
        tvPeriodConsumption.setText(formatNumber(currentPeriodKwh)
                + " kWh sur " + selectedPeriodDays + " jours");
        tvElectricityTrend.setText(formatTrend(currentPeriodKwh, previousPeriodKwh));
        tvComparisonSummary.setText(buildComparisonSummary(currentPeriodKwh, previousPeriodKwh, difference));
        tvDistributionSummary.setText(buildDistributionSummary(safeDevices, dailyKwh));

        // Mise à jour des graphiques
        updateBarChart(previousPeriodKwh, currentPeriodKwh);
        updatePieChart(safeDevices);
    }

    /**
     * Met à jour le graphique à barres (comparaison période précédente / actuelle)
     */
    private void updateBarChart(double previousPeriodKwh, double currentPeriodKwh) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) previousPeriodKwh));  // Barre 0 : période précédente
        entries.add(new BarEntry(1, (float) currentPeriodKwh));   // Barre 1 : période actuelle

        BarDataSet dataSet = new BarDataSet(entries, "Consommation");
        dataSet.setColors(new int[]{
                Color.parseColor("#9CA3AF"),  // Gris pour la précédente
                Color.parseColor("#FF6B3D")   // Orange pour l'actuelle
        });
        dataSet.setValueTextColor(Color.parseColor("#333333"));
        dataSet.setValueTextSize(11f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.45f);  // Largeur des barres
        barChart.setData(data);

        // Étiquettes des axes
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Précédente", "Actuelle"}
        ));

        barChart.animateY(600);  // Animation de 600ms
        barChart.invalidate();   // Rafraîchissement
    }

    /**
     * Met à jour le graphique camembert (répartition par type d'appareil)
     */
    private void updatePieChart(List<DeviceEntity> devices) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        Map<String, Double> consumptionByType = new LinkedHashMap<>();  // Conserve l'ordre

        // Agrégation de la consommation par type d'appareil
        for (DeviceEntity device : devices) {
            String label = resolveDeviceLabel(device);  // Normalisation du type
            Double currentValue = consumptionByType.get(label);
            consumptionByType.put(label, (currentValue == null ? 0 : currentValue)
                    + Math.max(0, device.getDailyConsumptionKwh()));
        }

        // Création des entrées pour le graphique
        for (Map.Entry<String, Double> entry : consumptionByType.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            return;
        }

        // Configuration du jeu de données
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#FF6B3D"),  // Orange
                Color.parseColor("#14B8A6"),  // Turquoise
                Color.parseColor("#2563EB"),  // Bleu
                Color.parseColor("#F59E0B"),  // Jaune
                Color.parseColor("#4CAF50"),  // Vert
                Color.parseColor("#7C3AED")   // Violet
        });
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setSliceSpace(2f);  // Espacement entre les parts

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.animateY(600);  // Animation
        pieChart.invalidate();   // Rafraîchissement
    }

    /**
     * Calcule la consommation journalière totale de tous les appareils
     */
    private double calculateDailyKwh(List<DeviceEntity> devices) {
        double total = 0;
        for (DeviceEntity device : devices) {
            total += Math.max(0, device.getDailyConsumptionKwh());  // Évite les valeurs négatives
        }
        return total;
    }

    /**
     * Calcule la consommation estimée de la période précédente
     * Basé sur les appareils créés avant le début de la période
     */
    private double calculatePreviousPeriodKwh(List<DeviceEntity> devices, int periodDays) {
        long currentPeriodStart = System.currentTimeMillis() - (periodDays * DAY_MS);
        double previousDailyKwh = 0;

        for (DeviceEntity device : devices) {
            if (device.getCreatedAt() <= currentPeriodStart) {
                previousDailyKwh += Math.max(0, device.getDailyConsumptionKwh());
            }
        }

        return previousDailyKwh * periodDays;
    }

    /**
     * Construit le texte de résumé comparatif (augmentation/diminution)
     */
    private String buildComparisonSummary(double currentPeriodKwh, double previousPeriodKwh, double difference) {
        String periodLabel = selectedPeriodDays == PERIOD_WEEK_DAYS ? "7 derniers jours" : "30 derniers jours";

        if (currentPeriodKwh == 0 && previousPeriodKwh == 0) {
            return "Aucune consommation utilisateur disponible pour le moment.";
        }
        if (previousPeriodKwh == 0) {
            return periodLabel + ": " + formatNumber(currentPeriodKwh)
                    + " kWh estimés. Pas encore de période précédente comparable.";
        }

        String direction = difference >= 0 ? "hausse" : "baisse";
        return periodLabel + ": " + formatNumber(currentPeriodKwh)
                + " kWh, soit " + formatNumber(Math.abs(difference))
                + " kWh de " + direction + " vs période précédente.";
    }

    /**
     * Construit le texte de résumé pour la répartition par appareil
     */
    private String buildDistributionSummary(List<DeviceEntity> devices, double dailyKwh) {
        if (devices.isEmpty() || dailyKwh == 0) {
            return "Aucun appareil enregistré pour calculer la répartition.";
        }
        return devices.size() + " appareil(s) suivis, " + formatNumber(dailyKwh * selectedPeriodDays)
                + " kWh estimés sur la période.";
    }

    /**
     * Formate le texte de tendance (pourcentage de hausse/baisse)
     */
    private String formatTrend(double currentPeriodKwh, double previousPeriodKwh) {
        if (currentPeriodKwh == 0 && previousPeriodKwh == 0) {
            return "Aucune donnée";
        }
        if (previousPeriodKwh == 0) {
            return "Nouveau suivi";
        }

        double trend = ((currentPeriodKwh - previousPeriodKwh) / previousPeriodKwh) * 100.0;
        if (trend > 0) {
            return "+" + formatNumber(trend) + "%";
        }
        return formatNumber(trend) + "%";
    }

    /**
     * Affiche un état vide (aucune donnée disponible)
     */
    private void showEmptyState(String message) {
        tvElectricityValue.setText("0 kWh/jour");
        tvPeriodConsumption.setText("0 kWh sur la période");
        tvElectricityTrend.setText("Aucune donnée");
        tvComparisonSummary.setText(message);
        tvDistributionSummary.setText(message);
        barChart.clear();
        pieChart.clear();
    }

    /**
     * Convertit le type d'appareil en libellé lisible en français
     */
    private String resolveDeviceLabel(DeviceEntity device) {
        String normalized = normalize(device.getType());

        if (normalized.contains("refrigerateur") || normalized.contains("frigo")) return "Réfrigérateur";
        if (normalized.contains("lave-vaisselle") || normalized.contains("lave vaisselle")) return "Lave-vaisselle";
        if (normalized.contains("lave-linge") || normalized.contains("lave linge") || normalized.contains("machine")) return "Lave-linge";
        if (normalized.contains("television") || normalized.contains("tv")) return "TV";
        if (normalized.contains("ordinateur") || normalized.contains("pc")) return "Ordinateur";
        if (normalized.contains("chauffage") || normalized.contains("radiateur")) return "Chauffage";
        if (normalized.contains("clim")) return "Climatisation";
        if (normalized.contains("lampe") || normalized.contains("ampoule") || normalized.contains("lumiere")) return "Lumières";

        if (device.getType() == null || device.getType().trim().isEmpty()) return "Autre";
        return device.getType().trim();
    }

    /**
     * Normalise une chaîne (supprime les accents et met en minuscules)
     */
    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase(Locale.FRANCE);
    }

    /**
     * Formate un nombre à la française (virgule comme séparateur décimal)
     */
    private String formatNumber(double value) {
        DecimalFormat format = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.FRANCE));
        return format.format(value);
    }

    /**
     * Met à jour le style des boutons (actif = orange, inactif = gris)
     */
    private void updateButtonStyle(Button active, Button inactive) {
        active.setBackgroundResource(R.drawable.button_orange_background);
        active.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        inactive.setBackgroundResource(R.drawable.button_gray_background);
        inactive.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
    }
}