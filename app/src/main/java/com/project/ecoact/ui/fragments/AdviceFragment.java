package com.project.ecoact.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.project.ecoact.R;
import com.project.ecoact.data.repository.AdviceRepository;
import com.project.ecoact.data.repository.HabitRepository;
import com.project.ecoact.data.repository.DeviceRepository;
import com.project.ecoact.util.SessionManager;

public class AdviceFragment extends Fragment {

    private TextView tvAdvice, tvError, tvTitle;
    private Button btnGenerate;
    private ProgressBar progressBar;

    private AdviceRepository adviceRepository;
    private HabitRepository habitRepository;
    private DeviceRepository deviceRepository;
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
        btnGenerate = view.findViewById(R.id.btn_generate_advice);
        progressBar = view.findViewById(R.id.progress_advice);

        adviceRepository = new AdviceRepository();
        habitRepository = new HabitRepository(requireActivity().getApplication());
        deviceRepository = new DeviceRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        btnGenerate.setOnClickListener(v -> generateAdvice());
    }

    private void generateAdvice() {
        showLoading(true);
        tvError.setVisibility(View.GONE);
        tvAdvice.setText("");

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
                getActivity().runOnUiThread(() -> {
                    adviceRepository.getAdvice(habitsData, devicesData, new AdviceRepository.AdviceCallback() {
                        @Override
                        public void onSuccess(String advice) {
                            showLoading(false);
                            tvAdvice.setText(advice);
                        }

                        @Override
                        public void onError(String error) {
                            showLoading(false);
                            tvError.setText("Erreur : " + error);
                            tvError.setVisibility(View.VISIBLE);
                        }
                    });
                });
            }
        }).start();
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!loading);
    }
}