package com.project.ecoact.data.repository;

import android.app.Application;

import com.project.ecoact.data.dao.DeviceDao;
import com.project.ecoact.data.databaseConf.AppDatabase;
import com.project.ecoact.data.entity.DeviceEntity;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

// Repository chargé de gérer les opérations liées aux appareils
// Il sert d'intermédiaire entre le reste de l'application et le DAO DeviceDao
public class DeviceRepository {
    // DAO permettant d'exécuter les requêtes SQL sur la table devices
    private final DeviceDao deviceDao;

    // Constructeur du repository
    // Il récupère l'instance de la base de données puis initialise le DAO
    public DeviceRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        deviceDao = db.deviceDao();
    }

    // Récupère de façon synchronisée tous les appareils d'un utilisateur
    // Comme Room ne doit pas être utilisé directement sur le thread principal,
    // la requête est exécutée dans un nouveau Thread.
    public List<DeviceEntity> getDevicesByUserSync(long userId) {
        // AtomicReference permet de stocker le résultat récupéré dans le Thread
        AtomicReference<List<DeviceEntity>> devices = new AtomicReference<>();

        // Crée un thread qui exécute la requête du DAO
        Thread thread = new Thread(() -> devices.set(deviceDao.getDevicesByUser(userId)));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return devices.get();
    }

    // Insère un nouvel appareil dans la base de données.
    // Retourne l'id généré pour l'appareil inséré.
    public long insertDevice(DeviceEntity device) throws InterruptedException {
        // Tableau utilisé pour récupérer l'id généré depuis le Thread.
        final long[] deviceId = new long[1];

        // Thread pour exécuter la requere DAO
        Thread thread = new Thread(() -> deviceId[0] = deviceDao.insert(device));

        // Lancer le thread
        thread.start();

        // Attendre qu'il termine
        thread.join();
        return deviceId[0];
    }

    // Met à jour un appareil existant dans la base de données
    public void updateDevice(DeviceEntity device) throws InterruptedException {
        Thread thread = new Thread(() -> deviceDao.update(device));
        thread.start();
        thread.join();
    }

    // Supprime un appareil de la base de données à partir de son objet DeviceEntity.
    public void deleteDevice(DeviceEntity device) throws InterruptedException {
        Thread thread = new Thread(() -> deviceDao.delete(device));
        thread.start();
        thread.join();
    }

    // Supprime un appareil grâce à son id,
    // seulement s'il appartient à l'utilisateur donné
    public void deleteDeviceById(long id, long userId) throws InterruptedException {
        Thread thread = new Thread(() -> deviceDao.deleteById(id, userId));
        thread.start();
        thread.join();
    }

    // Génère un résumé textuel des appareils enregistrés pour un utilisateur.
    public String getDevicesSummary(Long userId) {
        // Récupère la liste des appareils de l'utilisateur.
        List<DeviceEntity> devices = getDevicesByUserSync(userId);
    if (devices == null || devices.isEmpty()) {
        return "Aucun appareil enregistré";
    }
    StringBuilder summary = new StringBuilder();
    for (DeviceEntity device : devices) {
        summary.append(device.getType())
               .append(" ")
               .append(device.getReference())
               .append(" (")
               .append(device.getDailyUsageHours())
               .append("h/jour), ");
    }
    return summary.toString();
}
}
