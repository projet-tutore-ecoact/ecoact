package com.project.ecoact.data.repository;

import android.app.Application;

import com.project.ecoact.data.dao.EcoProductDao;
import com.project.ecoact.data.databaseConf.AppDatabase;
import com.project.ecoact.data.entity.DeviceEntity;
import com.project.ecoact.data.entity.EcoProductEntity;
import com.project.ecoact.data.entity.HabitEntity;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Repository qui gere les produits eco et les recommandations
public class EcoProductRepository {
    // Nombre maximum de produits recommandés
    private static final int MAX_RECOMMENDATIONS = 4;

    // DAO utilise pour acceder aux produits en base de donnees
    private final EcoProductDao ecoProductDao;

    // Recupere la base de donnees et initialise le DAO
    public EcoProductRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        ecoProductDao = db.ecoProductDao();
    }

    // Ajoute les produits par defaut si la base n'en contient pas assez
    public void seedDefaultsIfNeeded() {
        List<EcoProductEntity> defaults = buildDefaultProducts();
        if (ecoProductDao.countProducts() >= defaults.size()) {
            return;
        }
        ecoProductDao.insertAll(defaults);
    }

    // Retourne des produits recommandes selon les appareils et habitudes
    public RecommendationResult getRecommendations(List<DeviceEntity> devices, List<HabitEntity> habits) {
        seedDefaultsIfNeeded();

        // Evite les erreurs si les listes sont nulles
        List<DeviceEntity> safeDevices = devices == null ? new ArrayList<>() : devices;
        List<HabitEntity> safeHabits = habits == null ? new ArrayList<>() : habits;

        // Si le profil est vide, on ne peut pas proposer de vraie recommandation
        if (safeDevices.isEmpty() && safeHabits.isEmpty()) {
            return new RecommendationResult(
                    new ArrayList<>(),
                    "Ajoutez vos appareils et habitudes pour afficher des suggestions vraiment adaptées.",
                    false
            );
        }

        // Calcule les principaux indicateurs du profil utilisateur
        double yearlyConsumptionKwh = calculateYearlyConsumption(safeDevices);
        double maxDailyConsumptionKwh = calculateMaxDailyConsumption(safeDevices);
        int habitScore = calculateHabitScore(safeHabits);
        boolean hasCompleteProfile = !safeDevices.isEmpty() && !safeHabits.isEmpty();
        boolean hasEcoHabits = safeHabits.isEmpty() || habitScore >= 80;
        boolean hasLowConsumption = !safeDevices.isEmpty()
                && yearlyConsumptionKwh <= 650
                && maxDailyConsumptionKwh <= 1.6;

        // Si le profil est deja tres bon, on evite un achat inutile
        if (hasCompleteProfile && hasEcoHabits && hasLowConsumption) {
            return new RecommendationResult(
                    new ArrayList<>(),
                    "Tu es au top : ton profil est déjà sobre, je ne te suggère pas d'achat inutile.",
                    true
            );
        }

        List<String> categories = new ArrayList<>();
        addDeviceBasedCategories(categories, safeDevices);
        addHabitBasedCategories(categories, safeHabits);

        // Si rien de precis n'est trouve, on propose des categories utiles
        if (categories.isEmpty() && yearlyConsumptionKwh > 900) {
            addUnique(categories, "refrigerateur");
            addUnique(categories, "lave_linge");
            addUnique(categories, "lave_vaisselle");
            addUnique(categories, "chauffage");
        }

        List<EcoProductEntity> products = categories.isEmpty()
                ? new ArrayList<>()
                : ecoProductDao.getProductsForCategories(categories);

        List<EcoProductEntity> selectedProducts = keepBestProductPerCategory(products);
        String message = selectedProducts.isEmpty()
                ? "Ton profil est plutôt bon sur les signaux disponibles. Je ne te pousse pas à acheter sans besoin clair."
                : "Sélection partenaire basée sur tes appareils, ta consommation et tes habitudes.";

        return new RecommendationResult(selectedProducts, message, false);
    }

    // Cree la liste des produits disponibles par defaut
    private static List<EcoProductEntity> buildDefaultProducts() {
        List<EcoProductEntity> products = new ArrayList<>();

        products.add(new EcoProductEntity(
                "refrigerateur",
                "Bosch",
                "KGN39AIAT",
                "Réfrigérateur combiné Bosch KGN39AIAT",
                "A",
                "104 kWh/an",
                "Non communiqué",
                "Lien partenaire",
                "https://www.compeco.fr/p/refrigerateur-bosch-kgn39aiat/",
                "Adapté si le réfrigérateur pèse lourd dans la consommation annuelle.",
                100
        ));
        products.add(new EcoProductEntity(
                "refrigerateur",
                "Siemens",
                "KG39NAIAT",
                "Réfrigérateur combiné Siemens KG39NAIAT",
                "A",
                "104 kWh/an",
                "Non communiqué",
                "Lien partenaire",
                "https://www.compeco.fr/p/refrigerateur-siemens-kg39naiat/",
                "Alternative sobre pour remplacer un ancien réfrigérateur énergivore.",
                95
        ));
        products.add(new EcoProductEntity(
                "lave_linge",
                "Bosch",
                "WGB244A2FR",
                "Lave-linge Bosch WGB244A2FR",
                "A",
                "25 kWh / 100 cycles",
                "9.2 / 10",
                "Lien partenaire",
                "https://www.compeco.fr/p/lave-linge-bosch-wgb244a2fr/",
                "Prioritaire si les cycles de lavage reviennent souvent dans ton profil.",
                100
        ));
        products.add(new EcoProductEntity(
                "lave_vaisselle",
                "Bosch",
                "SMI8TCS01E",
                "Lave-vaisselle Bosch SMI8TCS01E",
                "A",
                "54 kWh / 100 cycles",
                "9 / 10",
                "Lien partenaire",
                "https://www.compeco.fr/p/lave-vaisselle-bosch-smi8tcs01e/",
                "Utile si le lave-vaisselle est utilisé souvent ou à moitié vide.",
                100
        ));
        products.add(new EcoProductEntity(
                "veille",
                "Legrand",
                "050095",
                "Bloc multiprise Legrand 4 prises avec interrupteur",
                "Coupe les veilles",
                "Interrupteur + parafoudre",
                "Sans objet",
                "Lien partenaire",
                "https://chauffage-sanitaire.partedis.com/p/bloc-multiprise-4x2pt-15m-a-voyant-avec-parafoudre-legrand-144942",
                "Recommandé quand les appareils restent branchés ou allumés inutilement.",
                90
        ));
        products.add(new EcoProductEntity(
                "lumiere",
                "Philips",
                "82616071",
                "Ampoule LED E27 Philips 806 lm",
                "LED 7 W",
                "806 lm pour 7 W",
                "Sans objet",
                "Lien partenaire",
                "https://www.leroymerlin.fr/produits/ampoule-globe-125-mm-e27-806lm-7w-philips-82616071.html",
                "À proposer si les lumières restent allumées ou si les LED ne sont pas encore utilisées.",
                85
        ));
        products.add(new EcoProductEntity(
                "chauffage",
                "Netatmo",
                "NTH01-FR-EC",
                "Thermostat intelligent Netatmo",
                "Pilotage chauffage",
                "Programmation + suivi de consommation",
                "Sans objet",
                "Lien partenaire",
                "https://www.netatmo.com/fr-fr/smart-thermostat",
                "À envisager si le chauffage ou la climatisation domine la consommation.",
                92
        ));

        return products;
    }

    // Calcule la consommation annuelle totale des appareils.
    private double calculateYearlyConsumption(List<DeviceEntity> devices) {
        double total = 0;
        for (DeviceEntity device : devices) {
            total += Math.max(0, device.getDailyConsumptionKwh()) * 365;
        }
        return total;
    }

    // Trouve la plus grande consommation quotidienne.
    private double calculateMaxDailyConsumption(List<DeviceEntity> devices) {
        double max = 0;
        for (DeviceEntity device : devices) {
            max = Math.max(max, device.getDailyConsumptionKwh());
        }
        return max;
    }

    // Calcule le score des habitudes sur 100.
    private int calculateHabitScore(List<HabitEntity> habits) {
        int points = 0;
        int maxPoints = 0;
        for (HabitEntity habit : habits) {
            points += habit.getPoints();
            maxPoints += habit.getMaxPoints();
        }
        if (maxPoints == 0) {
            return 0;
        }
        return (int) Math.round((points * 100.0) / maxPoints);
    }

    // Ajoute des categories selon les appareils
    private void addDeviceBasedCategories(List<String> categories, List<DeviceEntity> devices) {
        for (DeviceEntity device : devices) {
            String type = normalize(device.getType() + " " + device.getReference());
            double dailyKwh = device.getDailyConsumptionKwh();

            if ((type.contains("refrigerateur") || type.contains("frigo")) && dailyKwh >= 0.8) {
                addUnique(categories, "refrigerateur");
            } else if ((type.contains("lave-linge") || type.contains("lave linge") || type.contains("machine")) && dailyKwh >= 0.4) {
                addUnique(categories, "lave_linge");
            } else if ((type.contains("lave-vaisselle") || type.contains("lave vaisselle")) && dailyKwh >= 0.6) {
                addUnique(categories, "lave_vaisselle");
            } else if ((type.contains("lampe") || type.contains("ampoule") || type.contains("lumiere")) && dailyKwh >= 0.1) {
                addUnique(categories, "lumiere");
            } else if ((type.contains("television") || type.contains("tv") || type.contains("console")
                    || type.contains("box") || type.contains("ordinateur") || type.contains("pc")) && dailyKwh >= 0.4) {
                addUnique(categories, "veille");
            } else if ((type.contains("chauffage") || type.contains("radiateur") || type.contains("clim")) && dailyKwh >= 1.0) {
                addUnique(categories, "chauffage");
            }
        }
    }

    // Ajoute des categories selon les habitudes
    private void addHabitBasedCategories(List<String> categories, List<HabitEntity> habits) {
        for (HabitEntity habit : habits) {
            if (habit.getMaxPoints() <= 0) {
                continue;
            }

            double ratio = habit.getPoints() / (double) habit.getMaxPoints();
            if (ratio >= 0.67) {
                continue;
            }

            String signal = normalize(habit.getHabitKey() + " " + habit.getCategory() + " " + habit.getQuestion());
            if (signal.contains("lumiere") || signal.contains("lights")) {
                addUnique(categories, "lumiere");
            }
            if (signal.contains("veille") || signal.contains("standby") || signal.contains("recharge")
                    || signal.contains("battery") || signal.contains("branche") || signal.contains("tv")
                    || signal.contains("console") || signal.contains("box") || signal.contains("ordinateur")) {
                addUnique(categories, "veille");
            }
            if (signal.contains("electromenager") || signal.contains("appliance")
                    || signal.contains("lave-linge") || signal.contains("lave linge") || signal.contains("machine")) {
                addUnique(categories, "lave_linge");
            }
            if (signal.contains("lave-vaisselle") || signal.contains("lave vaisselle")) {
                addUnique(categories, "lave_vaisselle");
            }
            if (signal.contains("chauffage") || signal.contains("radiateur") || signal.contains("clim")) {
                addUnique(categories, "chauffage");
            }
        }
    }

    // Garde le meilleur produit pour chaque categorie
    private List<EcoProductEntity> keepBestProductPerCategory(List<EcoProductEntity> products) {
        List<EcoProductEntity> selectedProducts = new ArrayList<>();
        Set<String> selectedCategories = new HashSet<>();

        for (EcoProductEntity product : products) {
            if (selectedProducts.size() >= MAX_RECOMMENDATIONS) {
                break;
            }
            if (selectedCategories.add(product.getCategory())) {
                selectedProducts.add(product);
            }
        }

        return selectedProducts;
    }

    // Ajoute une categorie seulement si elle n'existe pas deja
    private static void addUnique(List<String> categories, String category) {
        if (!categories.contains(category)) {
            categories.add(category);
        }
    }

    // Nettoie un texte pour comparer plus facilement
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        return normalized.toLowerCase(Locale.FRANCE);
    }

    // Resultat final d'une recommandation
    public static class RecommendationResult {
        // Produits recommandes
        private final List<EcoProductEntity> products;
        // Message affiche a l'utilisateur
        private final String message;
        // Indique si le profil est deja tres bon
        private final boolean topProfile;

        // Cree un resultat de recommandation
        public RecommendationResult(List<EcoProductEntity> products, String message, boolean topProfile) {
            this.products = products;
            this.message = message;
            this.topProfile = topProfile;
        }

        // Retourne les produits recommandes
        public List<EcoProductEntity> getProducts() {
            return products;
        }

        // Retourne le message
        public String getMessage() {
            return message;
        }

        // Retourne true si le profil est deja au top
        public boolean isTopProfile() {
            return topProfile;
        }
    }
}
