package com.project.ecoact.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.project.ecoact.R;
import com.project.ecoact.data.entity.DeviceEntity;
import com.project.ecoact.data.entity.HabitEntity;
import com.project.ecoact.data.entity.User;
import com.project.ecoact.data.repository.DeviceRepository;
import com.project.ecoact.data.repository.HabitRepository;
import com.project.ecoact.data.repository.UserRepository;
import com.project.ecoact.util.CarbonFootprintCalculator;
import com.project.ecoact.util.ImpactCo2ApiClient;
import com.project.ecoact.util.SessionManager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private SessionManager sessionManager;
    private UserRepository userRepository;
    private DeviceRepository deviceRepository;
    private HabitRepository habitRepository;

    private TextView tvUserEmail, tvUserName;
    private TextView tvProfileAnnualKwh, tvProfileCarbonFootprint, tvProfileHabitScore;
    private TextView tvCarbonApiStatus, tvCarbonUsageDetail, tvCarbonDevicesDetail, tvCarbonHabitsDetail;
    private Button btnLogout, btnRefreshCarbon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvProfileAnnualKwh = view.findViewById(R.id.tv_profile_annual_kwh);
        tvProfileCarbonFootprint = view.findViewById(R.id.tv_profile_carbon_footprint);
        tvProfileHabitScore = view.findViewById(R.id.tv_profile_habit_score);
        tvCarbonApiStatus = view.findViewById(R.id.tv_carbon_api_status);
        tvCarbonUsageDetail = view.findViewById(R.id.tv_carbon_usage_detail);
        tvCarbonDevicesDetail = view.findViewById(R.id.tv_carbon_devices_detail);
        tvCarbonHabitsDetail = view.findViewById(R.id.tv_carbon_habits_detail);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnRefreshCarbon = view.findViewById(R.id.btn_refresh_carbon);

        sessionManager = new SessionManager(requireContext());
        userRepository = new UserRepository(requireActivity().getApplication());
        deviceRepository = new DeviceRepository(requireActivity().getApplication());
        habitRepository = new HabitRepository(requireActivity().getApplication());

        tvUserName.setText("Chargement...");
        tvUserEmail.setText("");
        showCarbonLoading();

        loadUserInfoAsync();
        loadCarbonFootprintAsync();

        btnLogout.setOnClickListener(v -> logout());
        btnRefreshCarbon.setOnClickListener(v -> loadCarbonFootprintAsync());
    }

    private void loadUserInfoAsync() {
        new Thread(() -> {
            try {
                Long userId = sessionManager.getCurrentUserId();

                User user;
                if (userId != null) {
                    user = userRepository.getUserByIdSync(userId);
                } else {
                    user = null;
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (user != null) {
                            tvUserName.setText("Utilisateur: " + user.getFirstName() + " " + user.getLastName());
                            tvUserEmail.setText("Email: " + user.getEmail());
                        } else {
                            tvUserName.setText("Utilisateur: Non trouvé");
                            tvUserEmail.setText("Email: N/A");
                        }
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvUserName.setText("Erreur de chargement");
                        tvUserEmail.setText("");
                    });
                }
                e.printStackTrace();
            }
        }).start();
    }

    private void loadCarbonFootprintAsync() {
        Long userId = sessionManager.getCurrentUserId();
        if (userId == null) {
            showNoCarbonData("Connectez-vous pour calculer votre empreinte carbone.");
            return;
        }

        if (getActivity() != null) {
            getActivity().runOnUiThread(this::showCarbonLoading);
        }

        new Thread(() -> {
            try {
                List<DeviceEntity> devices = deviceRepository.getDevicesByUserSync(userId);
                List<HabitEntity> habits = habitRepository.getHabitsByUserSync(userId);

                ImpactCo2ApiClient apiClient = new ImpactCo2ApiClient();
                ImpactCo2ApiClient.CarbonReferenceData carbonReferences = apiClient.fetchCarbonReferences();

                CarbonFootprintCalculator calculator = new CarbonFootprintCalculator();
                CarbonFootprintCalculator.CarbonFootprintResult result =
                        calculator.calculate(devices, habits, carbonReferences);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> showCarbonResult(result, carbonReferences));
                }
            } catch (Exception e) {
                showNoCarbonData("Impossible de calculer l'empreinte carbone pour le moment.");
            }
        }).start();
    }

    private void showCarbonLoading() {
        tvProfileAnnualKwh.setText("...");
        tvProfileCarbonFootprint.setText("...");
        tvProfileHabitScore.setText("...");
        tvCarbonApiStatus.setText("Calcul en cours avec Impact CO2...");
        tvCarbonUsageDetail.setText("");
        tvCarbonDevicesDetail.setText("");
        tvCarbonHabitsDetail.setText("");
        btnRefreshCarbon.setEnabled(false);
    }

    private void showCarbonResult(
            CarbonFootprintCalculator.CarbonFootprintResult result,
            ImpactCo2ApiClient.CarbonReferenceData carbonReferences
    ) {
        tvProfileAnnualKwh.setText(formatNumber(result.annualKwh) + " kWh");
        tvProfileCarbonFootprint.setText(formatNumber(result.totalKgCo2e) + " kg");
        tvProfileHabitScore.setText(result.habitScorePercent + "%");

        tvCarbonApiStatus.setText(carbonReferences.getMessage());
        tvCarbonUsageDetail.setText("Usage appareils: "
                + formatNumber(result.usageKgCo2e) + " kg CO2e/an");
        tvCarbonDevicesDetail.setText("Fabrication amortie: "
                + formatNumber(result.deviceManufacturingKgCo2e) + " kg CO2e/an pour "
                + result.deviceCount + " appareil(s)");
        tvCarbonHabitsDetail.setText("Effet habitudes: -"
                + formatNumber(result.habitReductionKgCo2e) + " kg CO2e/an (score "
                + result.habitPoints + "/" + result.habitMaxPoints + ")");

        btnRefreshCarbon.setEnabled(true);
    }

    private void showNoCarbonData(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvProfileAnnualKwh.setText("0 kWh");
                tvProfileCarbonFootprint.setText("0 kg");
                tvProfileHabitScore.setText("0%");
                tvCarbonApiStatus.setText(message);
                tvCarbonUsageDetail.setText("");
                tvCarbonDevicesDetail.setText("");
                tvCarbonHabitsDetail.setText("");
                btnRefreshCarbon.setEnabled(true);
            });
        }
    }

    private String formatNumber(double value) {
        DecimalFormat format = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.FRANCE));
        return format.format(value);
    }

    private void logout() {
        sessionManager.logout();

        Toast.makeText(getContext(), "Déconnecté", Toast.LENGTH_SHORT).show();

        Navigation.findNavController(getView())
                .navigate(R.id.action_profileFragment_to_loginFragment);
    }
}
