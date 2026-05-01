package com.project.ecoact.util;

import com.project.ecoact.data.entity.DeviceEntity;
import com.project.ecoact.data.entity.HabitEntity;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class CarbonFootprintCalculator {
    private static final double DAYS_PER_YEAR = 365.0;
    private static final double ELECTRICITY_KG_CO2E_PER_KWH = 0.059;
    private static final double MAX_HABIT_REDUCTION_RATE = 0.20;

    public CarbonFootprintResult calculate(
            List<DeviceEntity> devices,
            List<HabitEntity> habits,
            ImpactCo2ApiClient.CarbonReferenceData carbonReferences
    ) {
        CarbonFootprintResult result = new CarbonFootprintResult();

        if (devices != null) {
            for (DeviceEntity device : devices) {
                double annualKwh = device.getDailyConsumptionKwh() * DAYS_PER_YEAR;
                DeviceCarbonReference reference = getDeviceCarbonReference(device.getType(), carbonReferences);

                result.annualKwh += annualKwh;
                result.usageKgCo2e += annualKwh * ELECTRICITY_KG_CO2E_PER_KWH;
                result.deviceManufacturingKgCo2e += reference.footprintKgCo2e / reference.lifetimeYears;
                result.deviceCount++;
            }
        }

        calculateHabitScore(habits, result);

        double reductionRate = (result.habitScorePercent / 100.0) * MAX_HABIT_REDUCTION_RATE;
        result.habitReductionKgCo2e = result.usageKgCo2e * reductionRate;
        result.totalKgCo2e = Math.max(0, result.usageKgCo2e
                + result.deviceManufacturingKgCo2e
                - result.habitReductionKgCo2e);

        return result;
    }

    private void calculateHabitScore(List<HabitEntity> habits, CarbonFootprintResult result) {
        if (habits == null) {
            return;
        }

        for (HabitEntity habit : habits) {
            result.habitPoints += habit.getPoints();
            result.habitMaxPoints += habit.getMaxPoints();
            result.habitCount++;
        }

        if (result.habitMaxPoints > 0) {
            result.habitScorePercent = Math.round((result.habitPoints * 100f) / result.habitMaxPoints);
        }
    }

    private DeviceCarbonReference getDeviceCarbonReference(
            String deviceType,
            ImpactCo2ApiClient.CarbonReferenceData carbonReferences
    ) {
        String normalizedType = normalize(deviceType);

        if (normalizedType.contains("refrigerateur") || normalizedType.contains("frigo")) {
            return reference("refrigirateur", 338.7140334, 11, carbonReferences);
        }
        if (normalizedType.contains("lave-vaisselle")) {
            return reference("lavevaisselle", 460.79, 10, carbonReferences);
        }
        if (normalizedType.contains("lave-linge") || normalizedType.contains("machine")) {
            return reference("lavelinge", 513.42, 10, carbonReferences);
        }
        if (normalizedType.contains("four")) {
            return reference("fourelectrique", 272.60739996, 10, carbonReferences);
        }
        if (normalizedType.contains("micro")) {
            return reference("microondes", 121.35, 8, carbonReferences);
        }
        if (normalizedType.contains("bouilloire")) {
            return reference("bouilloire", 37.80339914, 7, carbonReferences);
        }
        if (normalizedType.contains("aspirateur")) {
            return reference("aspirateur", 73.43, 8, carbonReferences);
        }
        if (normalizedType.contains("clim")) {
            return reference("climatiseur", 421.747903, 10, carbonReferences);
        }
        if (normalizedType.contains("television") || normalizedType.contains("tv")) {
            return reference("television", 369.71186, 8, carbonReferences);
        }
        if (normalizedType.contains("ordinateur portable") || normalizedType.contains("laptop")) {
            return reference("ordinateurportable", 192.62004125, 5, carbonReferences);
        }
        if (normalizedType.contains("ordinateur") || normalizedType.contains("pc")) {
            return reference("ordinateurfixeparticulier", 300.37508, 5, carbonReferences);
        }
        if (normalizedType.contains("smartphone") || normalizedType.contains("telephone")) {
            return reference("smartphone", 80.155343125, 4, carbonReferences);
        }
        if (normalizedType.contains("tablette")) {
            return reference("tabletteclassique", 87.135525, 5, carbonReferences);
        }
        if (normalizedType.contains("box")) {
            return reference("box", 81.22576, 5, carbonReferences);
        }

        return new DeviceCarbonReference(100, 8);
    }

    private DeviceCarbonReference reference(
            String slug,
            double fallbackFootprintKgCo2e,
            int lifetimeYears,
            ImpactCo2ApiClient.CarbonReferenceData carbonReferences
    ) {
        double footprint = carbonReferences.getFootprint(slug, fallbackFootprintKgCo2e);
        return new DeviceCarbonReference(footprint, lifetimeYears);
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase(Locale.FRANCE);
    }

    private static class DeviceCarbonReference {
        private final double footprintKgCo2e;
        private final int lifetimeYears;

        private DeviceCarbonReference(double footprintKgCo2e, int lifetimeYears) {
            this.footprintKgCo2e = footprintKgCo2e;
            this.lifetimeYears = lifetimeYears;
        }
    }

    public static class CarbonFootprintResult {
        public double annualKwh;
        public double usageKgCo2e;
        public double deviceManufacturingKgCo2e;
        public double habitReductionKgCo2e;
        public double totalKgCo2e;
        public int deviceCount;
        public int habitCount;
        public int habitPoints;
        public int habitMaxPoints;
        public int habitScorePercent;
    }
}
