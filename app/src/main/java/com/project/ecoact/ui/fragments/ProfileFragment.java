package com.project.ecoact.ui.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final int POINTS_PER_LEVEL = 350;

    private SessionManager sessionManager;
    private UserRepository userRepository;
    private DeviceRepository deviceRepository;
    private HabitRepository habitRepository;

    private TextView tvUserEmail, tvUserName;
    private TextView tvProfileAnnualKwh, tvProfileCarbonFootprint, tvProfileHabitScore;
    private TextView tvCarbonApiStatus, tvCarbonUsageDetail, tvCarbonDevicesDetail, tvCarbonHabitsDetail;
    private TextView tvProfileLevel, tvProfilePoints, tvProfileNextLevel, tvEmptyBadges;
    private ProgressBar progressProfileLevel;
    private LinearLayout badgesContainer;
    private Button btnLogout, btnRefreshCarbon;
    private boolean firstResume = true;

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
        tvProfileLevel = view.findViewById(R.id.tv_profile_level);
        progressProfileLevel = view.findViewById(R.id.progress_profile_level);
        tvProfilePoints = view.findViewById(R.id.tv_profile_points);
        tvProfileNextLevel = view.findViewById(R.id.tv_profile_next_level);
        tvEmptyBadges = view.findViewById(R.id.tv_empty_badges);
        badgesContainer = view.findViewById(R.id.layout_badges_container);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnRefreshCarbon = view.findViewById(R.id.btn_refresh_carbon);

        sessionManager = new SessionManager(requireContext());
        userRepository = new UserRepository(requireActivity().getApplication());
        deviceRepository = new DeviceRepository(requireActivity().getApplication());
        habitRepository = new HabitRepository(requireActivity().getApplication());

        progressProfileLevel.setMax(100);
        tvUserName.setText("Chargement...");
        tvUserEmail.setText("");
        showCarbonLoading();

        loadUserInfoAsync();
        loadCarbonFootprintAsync();

        btnLogout.setOnClickListener(v -> logout());
        btnRefreshCarbon.setOnClickListener(v -> loadCarbonFootprintAsync());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (firstResume) {
            firstResume = false;
            return;
        }
        if (sessionManager != null) {
            loadCarbonFootprintAsync();
        }
    }

    private void loadUserInfoAsync() {
        new Thread(() -> {
            try {
                Long userId = sessionManager.getCurrentUserId();
                User user = userId == null ? null : userRepository.getUserByIdSync(userId);

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
        tvProfileLevel.setText("Calcul du niveau...");
        tvProfilePoints.setText("...");
        tvProfileNextLevel.setText("");
        progressProfileLevel.setProgress(0);
        badgesContainer.removeAllViews();
        tvEmptyBadges.setVisibility(View.VISIBLE);
        tvEmptyBadges.setText("Analyse des badges en cours...");
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

        showPointsAndBadges(result);
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
                showPointsAndBadges(new CarbonFootprintCalculator.CarbonFootprintResult());
                btnRefreshCarbon.setEnabled(true);
            });
        }
    }

    private void showPointsAndBadges(CarbonFootprintCalculator.CarbonFootprintResult result) {
        ProfileRewards rewards = calculateRewards(result);
        int level = (rewards.totalPoints / POINTS_PER_LEVEL) + 1;
        int currentLevelStart = (level - 1) * POINTS_PER_LEVEL;
        int nextLevelAt = level * POINTS_PER_LEVEL;
        int levelProgress = Math.round(((rewards.totalPoints - currentLevelStart) * 100f) / POINTS_PER_LEVEL);
        int pointsToNextLevel = nextLevelAt - rewards.totalPoints;

        tvProfileLevel.setText("Niveau " + level + " - " + getLevelName(level));
        tvProfilePoints.setText(rewards.totalPoints + " pts");
        tvProfileNextLevel.setText(pointsToNextLevel + " pts pour le niveau " + (level + 1));
        progressProfileLevel.setProgress(Math.max(0, Math.min(100, levelProgress)));

        badgesContainer.removeAllViews();
        tvEmptyBadges.setVisibility(rewards.badges.isEmpty() ? View.VISIBLE : View.GONE);
        if (rewards.badges.isEmpty()) {
            tvEmptyBadges.setText("Ajoutez des habitudes et appareils pour débloquer vos premiers badges.");
            return;
        }

        for (Badge badge : rewards.badges) {
            badgesContainer.addView(createBadgeView(badge));
        }
    }

    private ProfileRewards calculateRewards(CarbonFootprintCalculator.CarbonFootprintResult result) {
        ProfileRewards rewards = new ProfileRewards();

        rewards.totalPoints += result.habitPoints * 30;
        rewards.totalPoints += result.habitCount * 10;
        rewards.totalPoints += result.deviceCount * 20;

        if (result.habitScorePercent >= 80) {
            rewards.totalPoints += 250;
        } else if (result.habitScorePercent >= 60) {
            rewards.totalPoints += 150;
        }

        if (result.annualKwh > 0 && result.annualKwh <= 650) {
            rewards.totalPoints += 200;
        } else if (result.annualKwh > 0 && result.annualKwh <= 1000) {
            rewards.totalPoints += 100;
        }

        if (result.totalKgCo2e > 0 && result.totalKgCo2e <= 100) {
            rewards.totalPoints += 200;
        }

        if (result.habitCount > 0 || result.deviceCount > 0) {
            rewards.badges.add(new Badge("Premier pas", "Profil commencé avec vos premières données.", "+50 pts"));
            rewards.totalPoints += 50;
        }
        if (result.habitCount > 0 && result.deviceCount > 0) {
            rewards.badges.add(new Badge("Profil renseigné", "Appareils et habitudes sont tous les deux suivis.", "+80 pts"));
            rewards.totalPoints += 80;
        }
        if (result.deviceCount >= 3) {
            rewards.badges.add(new Badge("Chasseur de watts", "Au moins 3 appareils enregistrés.", "+120 pts"));
            rewards.totalPoints += 120;
        }
        if (result.habitScorePercent >= 60) {
            rewards.badges.add(new Badge("Habitudes solides", "Score d'habitudes supérieur ou égal à 60%.", "+150 pts"));
        }
        if (result.habitScorePercent >= 80) {
            rewards.badges.add(new Badge("Routine exemplaire", "Score d'habitudes supérieur ou égal à 80%.", "+250 pts"));
        }
        if (result.annualKwh > 0 && result.annualKwh <= 650) {
            rewards.badges.add(new Badge("Sobriété énergétique", "Consommation annuelle estimée sous 650 kWh.", "+200 pts"));
        }
        if (result.totalKgCo2e > 0 && result.totalKgCo2e <= 100) {
            rewards.badges.add(new Badge("Empreinte légère", "Empreinte estimée sous 100 kg CO2e/an.", "+200 pts"));
        }
        if (rewards.totalPoints >= 2450) {
            rewards.badges.add(new Badge("Eco-Expert", "Vous cumulez un très bon niveau global.", "Badge expert"));
        }

        return rewards;
    }

    private View createBadgeView(Badge badge) {
        LinearLayout badgeLayout = new LinearLayout(requireContext());
        badgeLayout.setOrientation(LinearLayout.VERTICAL);
        badgeLayout.setPadding(dp(12), dp(10), dp(12), dp(10));
        badgeLayout.setBackgroundResource(R.drawable.card_background_green);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(10));
        badgeLayout.setLayoutParams(params);

        TextView title = new TextView(requireContext());
        title.setText(badge.title + "  •  " + badge.rewardLabel);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        title.setTextSize(14);
        title.setTypeface(null, Typeface.BOLD);
        badgeLayout.addView(title);

        TextView description = new TextView(requireContext());
        description.setText(badge.description);
        description.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
        description.setTextSize(12);
        description.setPadding(0, dp(3), 0, 0);
        badgeLayout.addView(description);

        return badgeLayout;
    }

    private String getLevelName(int level) {
        if (level >= 9) {
            return "Maître sobriété";
        }
        if (level >= 7) {
            return "Eco-Expert";
        }
        if (level >= 5) {
            return "Eco-Responsable";
        }
        if (level >= 4) {
            return "Eco-Actif";
        }
        if (level >= 3) {
            return "Gardien des watts";
        }
        if (level >= 2) {
            return "Eco-Curieux";
        }
        return "Eco-Débutant";
    }

    private String formatNumber(double value) {
        DecimalFormat format = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.FRANCE));
        return format.format(value);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void logout() {
        sessionManager.logout();

        Toast.makeText(getContext(), "Déconnecté", Toast.LENGTH_SHORT).show();

        Navigation.findNavController(getView())
                .navigate(R.id.action_profileFragment_to_loginFragment);
    }

    private static class ProfileRewards {
        private int totalPoints;
        private final List<Badge> badges = new ArrayList<>();
    }

    private static class Badge {
        private final String title;
        private final String description;
        private final String rewardLabel;

        private Badge(String title, String description, String rewardLabel) {
            this.title = title;
            this.description = description;
            this.rewardLabel = rewardLabel;
        }
    }
}
