package com.project.ecoact.data.repository;

import android.app.Application;

import com.project.ecoact.data.dao.DeviceDao;
import com.project.ecoact.data.databaseConf.AppDatabase;
import com.project.ecoact.data.entity.DeviceEntity;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DeviceRepository {
    private final DeviceDao deviceDao;

    public DeviceRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        deviceDao = db.deviceDao();
    }

    public List<DeviceEntity> getDevicesByUserSync(long userId) {
        AtomicReference<List<DeviceEntity>> devices = new AtomicReference<>();
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

    public long insertDevice(DeviceEntity device) throws InterruptedException {
        final long[] deviceId = new long[1];
        Thread thread = new Thread(() -> deviceId[0] = deviceDao.insert(device));
        thread.start();
        thread.join();
        return deviceId[0];
    }

    public void updateDevice(DeviceEntity device) throws InterruptedException {
        Thread thread = new Thread(() -> deviceDao.update(device));
        thread.start();
        thread.join();
    }

    public void deleteDevice(DeviceEntity device) throws InterruptedException {
        Thread thread = new Thread(() -> deviceDao.delete(device));
        thread.start();
        thread.join();
    }

    public void deleteDeviceById(long id, long userId) throws InterruptedException {
        Thread thread = new Thread(() -> deviceDao.deleteById(id, userId));
        thread.start();
        thread.join();
    }
}
