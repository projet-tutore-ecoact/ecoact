package com.project.ecoact.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.project.ecoact.R;
import com.project.ecoact.data.databaseConf.AppDatabase;
import com.project.ecoact.data.DataInitializer;
import com.project.ecoact.data.entity.EnergyEntity;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private TextView tvElectricityValue, tvGasValue, tvWaterValue;
    private TextView tvElectricityTrend, tvGasTrend, tvWaterTrend;
    private Button btnWeek, btnMonth;
    private BarChart barChart;
    private PieChart pieChart;
    private AppDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser la base de données
        database = AppDatabase.getInstance(getContext());

        // Initialiser les données de test
        DataInitializer.initData(getContext());

        initViews(view);
        setupCharts();
        setupButtons();

        // Charger les données
        loadDataFromDatabase("week");
        updateButtonStyle(btnWeek, btnMonth);
    }

    private void initViews(View view) {
        tvElectricityValue = view.findViewById(R.id.tv_electricity_value);
        tvGasValue = view.findViewById(R.id.tv_gas_value);
        tvWaterValue = view.findViewById(R.id.tv_water_value);
        tvElectricityTrend = view.findViewById(R.id.tv_electricity_trend);
        tvGasTrend = view.findViewById(R.id.tv_gas_trend);
        tvWaterTrend = view.findViewById(R.id.tv_water_trend);
        btnWeek = view.findViewById(R.id.btn_week);
        btnMonth = view.findViewById(R.id.btn_month);

        // Initialiser les graphiques
        barChart = new BarChart(getContext());
        pieChart = new PieChart(getContext());

        // Ajouter les graphiques aux conteneurs
        ViewGroup chartContainer = view.findViewById(R.id.chart_container);
        chartContainer.removeAllViews();
        chartContainer.addView(barChart);

        ViewGroup pieContainer = view.findViewById(R.id.pie_container);
        pieContainer.removeAllViews();
        pieContainer.addView(pieChart);
    }

    private void setupCharts() {
        // Configuration du BarChart
        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.animateY(1000);

        // Configuration du PieChart
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.animateY(1000);
    }

    private void loadDataFromDatabase(String period) {
        new Thread(() -> {
            try {
                EnergyEntity electricity = database.energyDao().getByTypeAndPeriod("electricite", period);
                EnergyEntity gas = database.energyDao().getByTypeAndPeriod("gaz", period);
                EnergyEntity water = database.energyDao().getByTypeAndPeriod("eau", period);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Mettre à jour les cartes
                        if (electricity != null) {
                            tvElectricityValue.setText((int) electricity.value + " kWh");
                            tvElectricityTrend.setText(formatTrend(electricity.trend));
                        }
                        if (gas != null) {
                            tvGasValue.setText((int) gas.value + " m³");
                            tvGasTrend.setText(formatTrend(gas.trend));
                        }
                        if (water != null) {
                            tvWaterValue.setText((int) water.value + " m³");
                            tvWaterTrend.setText(formatTrend(water.trend));
                        }

                        // Mettre à jour les graphiques
                        updateBarChart(electricity, gas, water);
                        updatePieChart(electricity, gas, water);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateBarChart(EnergyEntity electricity, EnergyEntity gas, EnergyEntity water) {
        ArrayList<BarEntry> entries = new ArrayList<>();

        if (electricity != null) entries.add(new BarEntry(0, (float) electricity.value));
        if (gas != null) entries.add(new BarEntry(1, (float) gas.value));
        if (water != null) entries.add(new BarEntry(2, (float) water.value));

        BarDataSet dataSet = new BarDataSet(entries, "Consommation");
        dataSet.setColors(new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#FF9800"), Color.parseColor("#2196F3")});
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Labels pour l'axe X
        String[] labels = {"Électricité", "Gaz", "Eau"};
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);

        barChart.invalidate();
    }

    private void updatePieChart(EnergyEntity electricity, EnergyEntity gas, EnergyEntity water) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (electricity != null) entries.add(new PieEntry((float) electricity.value, "Électricité"));
        if (gas != null) entries.add(new PieEntry((float) gas.value, "Gaz"));
        if (water != null) entries.add(new PieEntry((float) water.value, "Eau"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private String formatTrend(double trend) {
        if (trend > 0) {
            return "+" + (int) trend + "%";
        } else {
            return (int) trend + "%";
        }
    }

    private void setupButtons() {
        btnWeek.setOnClickListener(v -> {
            loadDataFromDatabase("week");
            updateButtonStyle(btnWeek, btnMonth);
        });

        btnMonth.setOnClickListener(v -> {
            loadDataFromDatabase("month");
            updateButtonStyle(btnMonth, btnWeek);
        });
    }

    private void updateButtonStyle(Button active, Button inactive) {
        active.setBackgroundResource(R.drawable.button_orange_background);
        active.setTextColor(getResources().getColor(R.color.white));
        inactive.setBackgroundResource(R.drawable.button_gray_background);
        inactive.setTextColor(getResources().getColor(R.color.text_gray));
    }
}