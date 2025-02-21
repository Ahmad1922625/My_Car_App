package com.MyCarApp.core

import android.car.VehiclePropertyIds
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.CarPropertyValue
import android.util.Log

class VehiclePropertyProviderImpl(private val carPropertyManager: CarPropertyManager) : VehiclePropertyProvider {

    private val TAG = "VehiclePropertyProviderImpl"

    private val carPropertyListener = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            Log.d(TAG, "🚨 Property changed: ${value.propertyId}, New Value: ${value.value}")
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int) {
            Log.d(TAG, "Error accessing property: $propertyId")
        }
    }

    override fun getDoorLockStatus(areaId: Int): Boolean? {
        return try {
            val value = carPropertyManager.getBooleanProperty(VehiclePropertyIds.DOOR_LOCK, areaId)
            Log.d(TAG, "DOOR_LOCK read as $value for area ID: $areaId")
            value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read DOOR_LOCK - ${e.message}")
            null
        }
    }

    override fun getDoorPosition(areaId: Int): Int? {
        return try {
            val value = carPropertyManager.getIntProperty(VehiclePropertyIds.DOOR_MOVE, areaId)
            Log.d(TAG, "DOOR_MOVE read as $value for area ID: $areaId")
            value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read DOOR_MOVE - ${e.message}")
            null
        }
    }

    override fun setDoorLock(areaId: Int, lock: Boolean) {
        try {
            Log.d(TAG, "Setting DOOR_LOCK to $lock for area ID: $areaId")
            carPropertyManager.setBooleanProperty(VehiclePropertyIds.DOOR_LOCK, areaId, lock)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set DOOR_LOCK - ${e.message}")
        }
    }

    override fun setDoorMove(areaId: Int, value: Int) {
        try {
            Log.d(TAG, "Setting DOOR_MOVE to $value for area ID: $areaId")
            carPropertyManager.setIntProperty(VehiclePropertyIds.DOOR_MOVE, areaId, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set DOOR_MOVE - ${e.message}")
        }
    }

    fun registerDoorPropertyCallback() {
        carPropertyManager.registerCallback(carPropertyListener, VehiclePropertyIds.DOOR_LOCK, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        Log.d(TAG, "Registered callback for DOOR_LOCK")
    }

    fun unregisterDoorPropertyCallback() {
        carPropertyManager.unregisterCallback(carPropertyListener, VehiclePropertyIds.DOOR_LOCK)
        Log.d(TAG, "Unregistered callback for DOOR_LOCK")
    }
}
