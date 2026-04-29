package com.project.ecoact.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.project.ecoact.R;
import com.project.ecoact.data.entity.DeviceEntity;
import com.project.ecoact.data.repository.DeviceRepository;
import com.project.ecoact.util.SessionManager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DevicesFragment extends Fragment {

    private TextView tvTotalConsumption, tvDeviceCount, tvEmptyDevices, tvFormTitle;
    private TextView tvCalculatedConsumption, tvDeviceError;
    private EditText etDeviceType, etDeviceReference, etDailyUsage;
    private Button btnAddDevice, btnSaveDevice, btnCancelDevice, btnCalculateConsumption;
    private LinearLayout layoutDeviceForm, devicesContainer;

    private DeviceRepository deviceRepository;
    private SessionManager sessionManager;
    private Long currentUserId;
    private DeviceEntity selectedDevice;
    private final List<DeviceEntity> devices = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        deviceRepository = new DeviceRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());
        currentUserId = sessionManager.getCurrentUserId();

        setupListeners();

        if (currentUserId == null) {
            btnAddDevice.setEnabled(false);
            tvEmptyDevices.setText("Connectez-vous pour gérer vos appareils.");
            return;
        }

        loadDevices();
    }

    private void initViews(View view) {
        tvTotalConsumption = view.findViewById(R.id.tv_total_consumption);
        tvDeviceCount = view.findViewById(R.id.tv_device_count);
        tvEmptyDevices = view.findViewById(R.id.tv_empty_devices);
        tvFormTitle = view.findViewById(R.id.tv_form_title);
        tvCalculatedConsumption = view.findViewById(R.id.tv_calculated_consumption);
        tvDeviceError = view.findViewById(R.id.tv_device_error);

        etDeviceType = view.findViewById(R.id.et_device_type);
        etDeviceReference = view.findViewById(R.id.et_device_reference);
        etDailyUsage = view.findViewById(R.id.et_daily_usage);

        btnAddDevice = view.findViewById(R.id.btn_add_device);
        btnSaveDevice = view.findViewById(R.id.btn_save_device);
        btnCancelDevice = view.findViewById(R.id.btn_cancel_device);
        btnCalculateConsumption = view.findViewById(R.id.btn_calculate_consumption);

        layoutDeviceForm = view.findViewById(R.id.layout_device_form);
        devicesContainer = view.findViewById(R.id.ll_devices_container);
    }

    private void setupListeners() {
        btnAddDevice.setOnClickListener(v -> showAddForm());
        btnCancelDevice.setOnClickListener(v -> hideForm());
        btnCalculateConsumption.setOnClickListener(v -> updateConsumptionPreview(true));
        btnSaveDevice.setOnClickListener(v -> saveDevice());

        TextWatcher previewWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateConsumptionPreview(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etDeviceType.addTextChangedListener(previewWatcher);
        etDeviceReference.addTextChangedListener(previewWatcher);
        etDailyUsage.addTextChangedListener(previewWatcher);
    }

    private void loadDevices() {
        new Thread(() -> {
            List<DeviceEntity> loadedDevices = deviceRepository.getDevicesByUserSync(currentUserId);
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(() -> {
                devices.clear();
                if (loadedDevices != null) {
                    devices.addAll(loadedDevices);
                }
                renderDevices();
            });
        }).start();
    }

    private void renderDevices() {
        devicesContainer.removeAllViews();

        double totalConsumption = 0;
        for (DeviceEntity device : devices) {
            totalConsumption += device.getDailyConsumptionKwh();
            devicesContainer.addView(createDeviceRow(device));
        }

        tvTotalConsumption.setText(formatNumber(totalConsumption) + " kWh/jour");
        tvDeviceCount.setText(formatDeviceCount(devices.size()));
        tvEmptyDevices.setVisibility(devices.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private View createDeviceRow(DeviceEntity device) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_light));
        row.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, dp(12), 0, 0);
        row.setLayoutParams(rowParams);

        TextView title = new TextView(requireContext());
        title.setText(device.getType());
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        title.setTextSize(16);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(title);

        TextView reference = new TextView(requireContext());
        reference.setText("Référence: " + device.getReference());
        reference.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
        reference.setTextSize(13);
        row.addView(reference);

        TextView consumption = new TextView(requireContext());
        consumption.setText(formatNumber(device.getDailyUsageHours()) + " h/jour - "
                + formatNumber(device.getDailyConsumptionKwh()) + " kWh/jour");
        consumption.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_orange));
        consumption.setTextSize(13);
        row.addView(consumption);

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionParams.setMargins(0, dp(10), 0, 0);
        actions.setLayoutParams(actionParams);

        Button editButton = new Button(requireContext());
        editButton.setText("Modifier");
        editButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
        editButton.setBackgroundResource(R.drawable.button_gray_background);
        editButton.setOnClickListener(v -> showEditForm(device));
        actions.addView(editButton, weightedButtonParams(true));

        Button deleteButton = new Button(requireContext());
        deleteButton.setText("Supprimer");
        deleteButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        deleteButton.setBackgroundResource(R.drawable.button_orange_background);
        deleteButton.setOnClickListener(v -> confirmDelete(device));
        actions.addView(deleteButton, weightedButtonParams(false));

        row.addView(actions);
        return row;
    }

    private LinearLayout.LayoutParams weightedButtonParams(boolean withRightMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(44), 1);
        if (withRightMargin) {
            params.setMargins(0, 0, dp(8), 0);
        }
        return params;
    }

    private void showAddForm() {
        selectedDevice = null;
        tvFormTitle.setText("Ajouter un appareil");
        clearForm();
        layoutDeviceForm.setVisibility(View.VISIBLE);
        btnAddDevice.setVisibility(View.GONE);
        updateConsumptionPreview(false);
    }

    private void showEditForm(DeviceEntity device) {
        selectedDevice = device;
        tvFormTitle.setText("Modifier un appareil");
        etDeviceType.setText(device.getType());
        etDeviceReference.setText(device.getReference());
        etDailyUsage.setText(formatNumber(device.getDailyUsageHours()));
        tvDeviceError.setVisibility(View.GONE);
        layoutDeviceForm.setVisibility(View.VISIBLE);
        btnAddDevice.setVisibility(View.GONE);
        updateConsumptionPreview(false);
    }

    private void hideForm() {
        selectedDevice = null;
        clearForm();
        layoutDeviceForm.setVisibility(View.GONE);
        btnAddDevice.setVisibility(View.VISIBLE);
    }

    private void clearForm() {
        etDeviceType.setText("");
        etDeviceReference.setText("");
        etDailyUsage.setText("");
        tvDeviceError.setVisibility(View.GONE);
        tvCalculatedConsumption.setText("Consommation estimée: 0 kWh/jour");
        setFormLoading(false);
    }

    private void saveDevice() {
        String type = etDeviceType.getText().toString().trim();
        String reference = etDeviceReference.getText().toString().trim();
        Double usageHours = parseUsageHours(etDailyUsage.getText().toString().trim());

        if (!validateInputs(type, reference, usageHours)) {
            return;
        }

        double dailyConsumption = calculateDailyConsumption(type, reference, usageHours);
        setFormLoading(true);

        new Thread(() -> {
            try {
                if (selectedDevice == null) {
                    DeviceEntity newDevice = new DeviceEntity(currentUserId, type, reference, usageHours, dailyConsumption);
                    deviceRepository.insertDevice(newDevice);
                } else {
                    selectedDevice.setType(type);
                    selectedDevice.setReference(reference);
                    selectedDevice.setDailyUsageHours(usageHours);
                    selectedDevice.setDailyConsumptionKwh(dailyConsumption);
                    selectedDevice.setUpdatedAt(System.currentTimeMillis());
                    deviceRepository.updateDevice(selectedDevice);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Appareil enregistré", Toast.LENGTH_SHORT).show();
                        hideForm();
                        loadDevices();
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        setFormLoading(false);
                        showError("Erreur lors de l'enregistrement");
                    });
                }
            }
        }).start();
    }

    private void confirmDelete(DeviceEntity device) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer l'appareil")
                .setMessage("Voulez-vous supprimer " + device.getType() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteDevice(device))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteDevice(DeviceEntity device) {
        new Thread(() -> {
            try {
                deviceRepository.deleteDeviceById(device.getId(), currentUserId);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Appareil supprimé", Toast.LENGTH_SHORT).show();
                        if (selectedDevice != null && selectedDevice.getId() == device.getId()) {
                            hideForm();
                        }
                        loadDevices();
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

    private boolean validateInputs(String type, String reference, Double usageHours) {
        if (type.isEmpty()) {
            showError("Veuillez renseigner le type de l'appareil");
            return false;
        }

        if (reference.isEmpty()) {
            showError("Veuillez renseigner la référence de l'appareil");
            return false;
        }

        if (usageHours == null) {
            showError("Veuillez renseigner une durée valide");
            return false;
        }

        if (usageHours <= 0 || usageHours > 24) {
            showError("La durée doit être comprise entre 0 et 24 heures");
            return false;
        }

        tvDeviceError.setVisibility(View.GONE);
        return true;
    }

    private void updateConsumptionPreview(boolean showValidationError) {
        String type = etDeviceType.getText().toString().trim();
        String reference = etDeviceReference.getText().toString().trim();
        Double usageHours = parseUsageHours(etDailyUsage.getText().toString().trim());

        if (usageHours == null || usageHours < 0) {
            tvCalculatedConsumption.setText("Consommation estimée: 0 kWh/jour");
            if (showValidationError) {
                showError("Veuillez renseigner une durée valide");
            }
            return;
        }

        double powerWatts = resolvePowerWatts(type, reference);
        double consumption = roundToTwoDecimals((powerWatts * usageHours) / 1000);
        tvCalculatedConsumption.setText("Consommation estimée: "
                + formatNumber(consumption) + " kWh/jour (" + formatNumber(powerWatts) + " W)");
        tvDeviceError.setVisibility(View.GONE);
    }

    private double calculateDailyConsumption(String type, String reference, double usageHours) {
        double powerWatts = resolvePowerWatts(type, reference);
        return roundToTwoDecimals((powerWatts * usageHours) / 1000);
    }

    private double resolvePowerWatts(String type, String reference) {
        Double powerFromReference = extractPowerWatts(reference);
        if (powerFromReference != null) {
            return powerFromReference;
        }

        String normalizedType = normalize(type);
        if (normalizedType.contains("refrigerateur") || normalizedType.contains("frigo")) return 50;
        if (normalizedType.contains("television") || normalizedType.contains("tv")) return 100;
        if (normalizedType.contains("ordinateur") || normalizedType.contains("pc")) return 90;
        if (normalizedType.contains("lave-linge") || normalizedType.contains("machine")) return 500;
        if (normalizedType.contains("lave-vaisselle")) return 1200;
        if (normalizedType.contains("four")) return 2000;
        if (normalizedType.contains("micro")) return 1000;
        if (normalizedType.contains("clim")) return 1000;
        if (normalizedType.contains("chauffage")) return 1500;
        if (normalizedType.contains("lampe") || normalizedType.contains("ampoule")) return 10;
        if (normalizedType.contains("aspirateur")) return 800;
        return 100;
    }

    private Double extractPowerWatts(String reference) {
        if (reference == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*(kw|w)(?!h)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(reference);
        if (!matcher.find()) {
            return null;
        }

        double value = Double.parseDouble(matcher.group(1).replace(",", "."));
        String unit = matcher.group(2).toLowerCase(Locale.FRANCE);
        return unit.equals("kw") ? value * 1000 : value;
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase(Locale.FRANCE);
    }

    private Double parseUsageHours(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatNumber(double value) {
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.FRANCE));
        return format.format(value);
    }

    private String formatDeviceCount(int count) {
        if (count <= 1) {
            return count + " appareil enregistré";
        }
        return count + " appareils enregistrés";
    }

    private void setFormLoading(boolean loading) {
        btnSaveDevice.setEnabled(!loading);
        btnCalculateConsumption.setEnabled(!loading);
        btnCancelDevice.setEnabled(!loading);
    }

    private void showError(String message) {
        tvDeviceError.setText(message);
        tvDeviceError.setVisibility(View.VISIBLE);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
