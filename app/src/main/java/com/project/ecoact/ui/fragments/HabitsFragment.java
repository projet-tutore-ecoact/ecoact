package com.project.ecoact.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.project.ecoact.R;
import com.project.ecoact.data.entity.HabitEntity;
import com.project.ecoact.data.repository.HabitRepository;
import com.project.ecoact.util.SessionManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HabitsFragment extends Fragment {

    private static final String[] FREQUENCIES = {"Toujours", "Souvent", "Parfois", "Jamais"};

    private TextView tvHabitPoints, tvHabitCount, tvHabitsPercent, tvEmptyHabits, tvEmptyScores, tvHabitError;
    private ProgressBar progressHabits;
    private Button btnAddHabit, btnSaveHabit, btnCancelHabit;
    private Spinner spinnerHabit, spinnerFrequency;
    private LinearLayout layoutHabitForm, habitsContainer, categoryScoresContainer;

    private HabitRepository habitRepository;
    private SessionManager sessionManager;
    private Long currentUserId;
    private final List<HabitEntity> habits = new ArrayList<>();
    private final List<HabitDefinition> habitDefinitions = buildHabitDefinitions();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habits, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupSpinners();

        habitRepository = new HabitRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());
        currentUserId = sessionManager.getCurrentUserId();

        setupListeners();

        if (currentUserId == null) {
            btnAddHabit.setEnabled(false);
            tvEmptyHabits.setText("Connectez-vous pour gérer vos habitudes.");
            tvEmptyScores.setText("Connectez-vous pour voir vos scores.");
            return;
        }

        loadHabits();
    }

    private void initViews(View view) {
        tvHabitPoints = view.findViewById(R.id.tv_habit_points);
        tvHabitCount = view.findViewById(R.id.tv_habit_count);
        tvHabitsPercent = view.findViewById(R.id.tv_habits_percent);
        tvEmptyHabits = view.findViewById(R.id.tv_empty_habits);
        tvEmptyScores = view.findViewById(R.id.tv_empty_scores);
        tvHabitError = view.findViewById(R.id.tv_habit_error);

        progressHabits = view.findViewById(R.id.progress_habits);
        btnAddHabit = view.findViewById(R.id.btn_add_habit);
        btnSaveHabit = view.findViewById(R.id.btn_save_habit);
        btnCancelHabit = view.findViewById(R.id.btn_cancel_habit);
        spinnerHabit = view.findViewById(R.id.spinner_habit);
        spinnerFrequency = view.findViewById(R.id.spinner_frequency);

        layoutHabitForm = view.findViewById(R.id.layout_habit_form);
        habitsContainer = view.findViewById(R.id.ll_habits_container);
        categoryScoresContainer = view.findViewById(R.id.ll_category_scores_container);
    }

    private void setupSpinners() {
        ArrayAdapter<HabitDefinition> habitAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_selected,
                habitDefinitions
        );
        habitAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerHabit.setAdapter(habitAdapter);

        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_selected,
                FREQUENCIES
        );
        frequencyAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerFrequency.setAdapter(frequencyAdapter);
    }

    private void setupListeners() {
        btnAddHabit.setOnClickListener(v -> showForm());
        btnCancelHabit.setOnClickListener(v -> hideForm());
        btnSaveHabit.setOnClickListener(v -> saveHabit());
    }

    private void loadHabits() {
        new Thread(() -> {
            List<HabitEntity> loadedHabits = habitRepository.getHabitsByUserSync(currentUserId);
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(() -> {
                habits.clear();
                if (loadedHabits != null) {
                    habits.addAll(loadedHabits);
                }
                renderHabits();
            });
        }).start();
    }

    private void renderHabits() {
        habitsContainer.removeAllViews();
        categoryScoresContainer.removeAllViews();

        int totalPoints = 0;
        int totalMaxPoints = 0;
        Map<String, CategoryScore> scoresByCategory = new LinkedHashMap<>();

        for (HabitEntity habit : habits) {
            totalPoints += habit.getPoints();
            totalMaxPoints += habit.getMaxPoints();

            CategoryScore categoryScore = scoresByCategory.get(habit.getCategory());
            if (categoryScore == null) {
                categoryScore = new CategoryScore();
                scoresByCategory.put(habit.getCategory(), categoryScore);
            }
            categoryScore.points += habit.getPoints();
            categoryScore.maxPoints += habit.getMaxPoints();

            habitsContainer.addView(createHabitRow(habit));
        }

        int globalPercent = calculatePercent(totalPoints, totalMaxPoints);
        tvHabitPoints.setText(totalPoints + " / " + totalMaxPoints + " pts");
        tvHabitCount.setText(formatHabitCount(habits.size()));
        progressHabits.setProgress(globalPercent);
        tvHabitsPercent.setText(globalPercent + "%");

        tvEmptyHabits.setVisibility(habits.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmptyScores.setVisibility(scoresByCategory.isEmpty() ? View.VISIBLE : View.GONE);

        for (Map.Entry<String, CategoryScore> entry : scoresByCategory.entrySet()) {
            categoryScoresContainer.addView(createCategoryScoreRow(entry.getKey(), entry.getValue()));
        }
    }

    private View createHabitRow(HabitEntity habit) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(12), dp(12), dp(12));
        row.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_light_orange));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, dp(12), 0, 0);
        row.setLayoutParams(rowParams);

        CheckBox checkBox = new CheckBox(requireContext());
        checkBox.setChecked(true);
        checkBox.setEnabled(false);
        checkBox.setText(habit.getQuestion());
        checkBox.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        row.addView(checkBox);

        TextView details = new TextView(requireContext());
        details.setText(habit.getCategory() + " - " + habit.getFrequency()
                + " - " + habit.getPoints() + "/" + habit.getMaxPoints() + " pts");
        details.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
        details.setTextSize(12);
        row.addView(details);

        Button deleteButton = new Button(requireContext());
        deleteButton.setText("Supprimer");
        deleteButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        deleteButton.setBackgroundResource(R.drawable.button_orange_background);
        deleteButton.setOnClickListener(v -> confirmDelete(habit));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(44)
        );
        buttonParams.setMargins(0, dp(8), 0, 0);
        row.addView(deleteButton, buttonParams);

        return row;
    }

    private View createCategoryScoreRow(String category, CategoryScore score) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, dp(12), 0, 0);
        row.setLayoutParams(rowParams);

        int percent = calculatePercent(score.points, score.maxPoints);

        TextView label = new TextView(requireContext());
        label.setText(category + " - " + percent + "% (" + score.points + "/" + score.maxPoints + " pts)");
        label.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        label.setTextSize(14);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(label);

        ProgressBar progressBar = new ProgressBar(
                requireContext(),
                null,
                android.R.attr.progressBarStyleHorizontal
        );
        progressBar.setMax(100);
        progressBar.setProgress(percent);

        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(8)
        );
        progressParams.setMargins(0, dp(6), 0, 0);
        row.addView(progressBar, progressParams);

        return row;
    }

    private void showForm() {
        tvHabitError.setVisibility(View.GONE);
        layoutHabitForm.setVisibility(View.VISIBLE);
        btnAddHabit.setVisibility(View.GONE);
    }

    private void hideForm() {
        tvHabitError.setVisibility(View.GONE);
        layoutHabitForm.setVisibility(View.GONE);
        btnAddHabit.setVisibility(View.VISIBLE);
        setFormLoading(false);
    }

    private void saveHabit() {
        HabitDefinition definition = (HabitDefinition) spinnerHabit.getSelectedItem();
        String frequency = (String) spinnerFrequency.getSelectedItem();

        if (definition == null || frequency == null) {
            showError("Veuillez choisir une habitude et une fréquence");
            return;
        }

        int points = calculatePoints(frequency, definition.inverted);
        HabitEntity habit = new HabitEntity(
                currentUserId,
                definition.key,
                definition.category,
                definition.question,
                frequency,
                definition.inverted,
                points,
                3
        );

        setFormLoading(true);

        new Thread(() -> {
            try {
                HabitEntity existingHabit = habitRepository.getHabitByKeySync(currentUserId, definition.key);
                if (existingHabit != null) {
                    showErrorOnUiThread("Cette habitude est déjà enregistrée");
                    return;
                }

                habitRepository.insertHabit(habit);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Habitude enregistrée", Toast.LENGTH_SHORT).show();
                        hideForm();
                        loadHabits();
                    });
                }
            } catch (Exception e) {
                showErrorOnUiThread("Erreur lors de l'enregistrement");
            }
        }).start();
    }

    private void confirmDelete(HabitEntity habit) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer l'habitude")
                .setMessage("Voulez-vous supprimer cette habitude ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteHabit(habit))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteHabit(HabitEntity habit) {
        new Thread(() -> {
            try {
                habitRepository.deleteHabitById(habit.getId(), currentUserId);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Habitude supprimée", Toast.LENGTH_SHORT).show();
                        loadHabits();
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private int calculatePoints(String frequency, boolean inverted) {
        int points;
        switch (frequency.toLowerCase(Locale.FRANCE)) {
            case "toujours":
                points = 3;
                break;
            case "souvent":
                points = 2;
                break;
            case "parfois":
                points = 1;
                break;
            default:
                points = 0;
                break;
        }

        return inverted ? 3 - points : points;
    }

    private int calculatePercent(int points, int maxPoints) {
        if (maxPoints == 0) {
            return 0;
        }
        return Math.round((points * 100f) / maxPoints);
    }

    private String formatHabitCount(int count) {
        if (count <= 1) {
            return count + " habitude enregistrée";
        }
        return count + " habitudes enregistrées";
    }

    private void setFormLoading(boolean loading) {
        btnSaveHabit.setEnabled(!loading);
        btnCancelHabit.setEnabled(!loading);
        spinnerHabit.setEnabled(!loading);
        spinnerFrequency.setEnabled(!loading);
    }

    private void showError(String message) {
        tvHabitError.setText(message);
        tvHabitError.setVisibility(View.VISIBLE);
    }

    private void showErrorOnUiThread(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                setFormLoading(false);
                showError(message);
            });
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private static List<HabitDefinition> buildHabitDefinitions() {
        List<HabitDefinition> definitions = new ArrayList<>();

        definitions.add(new HabitDefinition("lights_off", "Lumières",
                "J'éteins la lumière quand je quitte une pièce.", false));
        definitions.add(new HabitDefinition("lights_natural", "Lumières",
                "J'utilise la lumière naturelle quand c'est possible.", false));
        definitions.add(new HabitDefinition("lights_led", "Lumières",
                "J'utilise des ampoules LED.", false));
        definitions.add(new HabitDefinition("lights_left_on", "Lumières",
                "Je laisse parfois les lumières allumées inutilement.", true));

        definitions.add(new HabitDefinition("standby_off", "Appareils en veille",
                "J'éteins complètement mes appareils au lieu de les laisser en veille.", false));
        definitions.add(new HabitDefinition("standby_chargers", "Appareils en veille",
                "Je débranche les chargeurs quand ils ne servent pas.", false));
        definitions.add(new HabitDefinition("standby_power_strip", "Appareils en veille",
                "J'utilise une multiprise avec interrupteur.", false));
        definitions.add(new HabitDefinition("standby_tv_on", "Appareils en veille",
                "Je laisse la TV / console / box allumée même sans utilisation.", true));

        definitions.add(new HabitDefinition("usage_needed", "Utilisation des appareils",
                "Je lance les appareils énergivores seulement quand nécessaire.", false));
        definitions.add(new HabitDefinition("usage_not_together", "Utilisation des appareils",
                "J'évite d'utiliser plusieurs gros appareils en même temps.", false));
        definitions.add(new HabitDefinition("usage_heat_cool", "Utilisation des appareils",
                "Je limite le temps d'utilisation de la climatisation / chauffage électrique.", false));
        definitions.add(new HabitDefinition("usage_pc_all_day", "Utilisation des appareils",
                "Je laisse mon ordinateur allumé toute la journée.", true));

        definitions.add(new HabitDefinition("battery_unplug_phone", "Recharge et batterie",
                "Je débranche mon téléphone quand il est chargé.", false));
        definitions.add(new HabitDefinition("battery_no_night", "Recharge et batterie",
                "J'évite de charger mes appareils toute la nuit.", false));
        definitions.add(new HabitDefinition("battery_saver", "Recharge et batterie",
                "J'utilise le mode économie d'énergie.", false));
        definitions.add(new HabitDefinition("battery_left_plugged", "Recharge et batterie",
                "Je laisse souvent mes appareils branchés inutilement.", true));

        definitions.add(new HabitDefinition("appliance_full_machine", "Électroménager",
                "Je remplis bien la machine à laver avant de la lancer.", false));
        definitions.add(new HabitDefinition("appliance_eco_program", "Électroménager",
                "J'utilise les programmes éco.", false));
        definitions.add(new HabitDefinition("appliance_low_temp", "Électroménager",
                "Je lave à basse température quand c'est possible.", false));
        definitions.add(new HabitDefinition("appliance_half_empty", "Électroménager",
                "Je fais tourner le lave-linge / lave-vaisselle à moitié vide.", true));

        definitions.add(new HabitDefinition("shower_short", "Douche / eau chaude",
                "Je prends des douches courtes.", false));
        definitions.add(new HabitDefinition("shower_water_off", "Douche / eau chaude",
                "Je coupe l'eau quand je me savonne.", false));
        definitions.add(new HabitDefinition("shower_no_bath", "Douche / eau chaude",
                "J'évite les bains.", false));
        definitions.add(new HabitDefinition("shower_long_hot", "Douche / eau chaude",
                "Je prends souvent des douches longues et très chaudes.", true));

        return definitions;
    }

    private static class HabitDefinition {
        private final String key;
        private final String category;
        private final String question;
        private final boolean inverted;

        private HabitDefinition(String key, String category, String question, boolean inverted) {
            this.key = key;
            this.category = category;
            this.question = question;
            this.inverted = inverted;
        }

        @NonNull
        @Override
        public String toString() {
            return category + "\n" + question;
        }
    }

    private static class CategoryScore {
        private int points;
        private int maxPoints;
    }
}
