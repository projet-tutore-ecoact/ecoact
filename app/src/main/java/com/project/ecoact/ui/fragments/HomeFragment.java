package com.project.ecoact.ui.fragments;

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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final long DAY_MS = 24L * 60L * 60L * 1000L;
    private static final int PERIOD_WEEK_DAYS = 7;
    private static final int PERIOD_MONTH_DAYS = 30;

    private TextView tvElectricityValue, tvElectricityTrend, tvPeriodConsumption;
    private TextView tvComparisonSummary, tvDistributionSummary;
    private Button btnWeek, btnMonth;
    private BarChart barChart;
    private PieChart pieChart;
    private DeviceRepository deviceRepository;
    private SessionManager sessionManager;
    private int selectedPeriodDays = PERIOD_WEEK_DAYS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceRepository = new DeviceRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        setupCharts();
        setupButtons();
        updateButtonStyle(btnWeek, btnMonth);
        loadUserConsumption();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (deviceRepository != null) {
            loadUserConsumption();
        }
    }

    private void initViews(View view) {
        tvElectricityValue = view.findViewById(R.id.tv_electricity_value);
        tvElectricityTrend = view.findViewById(R.id.tv_electricity_trend);
        tvPeriodConsumption = view.findViewById(R.id.tv_period_consumption);
        tvComparisonSummary = view.findViewById(R.id.tv_comparison_summary);
        tvDistributionSummary = view.findViewById(R.id.tv_distribution_summary);
        btnWeek = view.findViewById(R.id.btn_week);
        btnMonth = view.findViewById(R.id.btn_month);

        barChart = new BarChart(requireContext());
        pieChart = new PieChart(requireContext());

        ViewGroup chartContainer = view.findViewById(R.id.chart_container);
        chartContainer.removeAllViews();
        chartContainer.addView(barChart, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        ViewGroup pieContainer = view.findViewById(R.id.pie_container);
        pieContainer.removeAllViews();
        pieContainer.addView(pieChart, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private void setupCharts() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setNoDataText("Ajoutez des appareils pour afficher l'évolution.");

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(62f);
        pieChart.setNoDataText("Ajoutez des appareils pour afficher la répartition.");
    }

    private void setupButtons() {
        btnWeek.setOnClickListener(v -> {
            selectedPeriodDays = PERIOD_WEEK_DAYS;
            updateButtonStyle(btnWeek, btnMonth);
            loadUserConsumption();
        });

        btnMonth.setOnClickListener(v -> {
            selectedPeriodDays = PERIOD_MONTH_DAYS;
            updateButtonStyle(btnMonth, btnWeek);
            loadUserConsumption();
        });
    }

    private void loadUserConsumption() {
        Long userId = sessionManager.getCurrentUserId();
        if (userId == null) {
            showEmptyState("Connectez-vous pour afficher votre consommation.");
            return;
        }

        new Thread(() -> {
            List<DeviceEntity> devices = deviceRepository.getDevicesByUserSync(userId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> renderConsumption(devices));
            }
        }).start();
    }

    private void renderConsumption(List<DeviceEntity> devices) {
        List<DeviceEntity> safeDevices = devices == null ? new ArrayList<>() : devices;
        double dailyKwh = calculateDailyKwh(safeDevices);
        double currentPeriodKwh = dailyKwh * selectedPeriodDays;
        double previousPeriodKwh = calculatePreviousPeriodKwh(safeDevices, selectedPeriodDays);
        double difference = currentPeriodKwh - previousPeriodKwh;

        tvElectricityValue.setText(formatNumber(dailyKwh) + " kWh/jour");
        tvPeriodConsumption.setText(formatNumber(currentPeriodKwh)
                + " kWh sur " + selectedPeriodDays + " jours");
        tvElectricityTrend.setText(formatTrend(currentPeriodKwh, previousPeriodKwh));
        tvComparisonSummary.setText(buildComparisonSummary(currentPeriodKwh, previousPeriodKwh, difference));
        tvDistributionSummary.setText(buildDistributionSummary(safeDevices, dailyKwh));

        updateBarChart(previousPeriodKwh, currentPeriodKwh);
        updatePieChart(safeDevices);
    }

    private void updateBarChart(double previousPeriodKwh, double currentPeriodKwh) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) previousPeriodKwh));
        entries.add(new BarEntry(1, (float) currentPeriodKwh));

        BarDataSet dataSet = new BarDataSet(entries, "Consommation");
        dataSet.setColors(new int[]{
                Color.parseColor("#9CA3AF"),
                Color.parseColor("#FF6B3D")
        });
        dataSet.setValueTextColor(Color.parseColor("#333333"));
        dataSet.setValueTextSize(11f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.45f);
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Précédente", "Actuelle"}
        ));
        barChart.animateY(600);
        barChart.invalidate();
    }

    private void updatePieChart(List<DeviceEntity> devices) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        Map<String, Double> consumptionByType = new LinkedHashMap<>();

        for (DeviceEntity device : devices) {
            String label = resolveDeviceLabel(device);
            Double currentValue = consumptionByType.get(label);
            consumptionByType.put(label, (currentValue == null ? 0 : currentValue)
                    + Math.max(0, device.getDailyConsumptionKwh()));
        }

        for (Map.Entry<String, Double> entry : consumptionByType.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#FF6B3D"),
                Color.parseColor("#14B8A6"),
                Color.parseColor("#2563EB"),
                Color.parseColor("#F59E0B"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#7C3AED")
        });
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.animateY(600);
        pieChart.invalidate();
    }

    private double calculateDailyKwh(List<DeviceEntity> devices) {
        double total = 0;
        for (DeviceEntity device : devices) {
            total += Math.max(0, device.getDailyConsumptionKwh());
        }
        return total;
    }

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

    private String buildDistributionSummary(List<DeviceEntity> devices, double dailyKwh) {
        if (devices.isEmpty() || dailyKwh == 0) {
            return "Aucun appareil enregistré pour calculer la répartition.";
        }
        return devices.size() + " appareil(s) suivis, " + formatNumber(dailyKwh * selectedPeriodDays)
                + " kWh estimés sur la période.";
    }

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

    private void showEmptyState(String message) {
        tvElectricityValue.setText("0 kWh/jour");
        tvPeriodConsumption.setText("0 kWh sur la période");
        tvElectricityTrend.setText("Aucune donnée");
        tvComparisonSummary.setText(message);
        tvDistributionSummary.setText(message);
        barChart.clear();
        pieChart.clear();
    }

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

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase(Locale.FRANCE);
    }

    private String formatNumber(double value) {
        DecimalFormat format = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.FRANCE));
        return format.format(value);
    }

    private void updateButtonStyle(Button active, Button inactive) {
        active.setBackgroundResource(R.drawable.button_orange_background);
        active.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        inactive.setBackgroundResource(R.drawable.button_gray_background);
        inactive.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
    }
}
