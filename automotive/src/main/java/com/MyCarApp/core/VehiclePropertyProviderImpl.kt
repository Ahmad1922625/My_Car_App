package com.MyCarApp.core

import android.car.hardware.property.CarPropertyManager
import android.car.hardware.CarPropertyValue
import android.content.Intent
import android.util.Log
import com.MyCarApp.MyCarApp
import com.MyCarApp.modules.door_control.DoorControlModule

class VehiclePropertyProviderImpl(private val carPropertyManager: CarPropertyManager) : VehiclePropertyProvider {

    private val TAG = "VehiclePropertyProviderImpl"
    private var doorLockCallback: CarPropertyManager.CarPropertyEventCallback? = null

    override fun getDoorLockStatus(areaId: Int): Boolean? {
        return try {
            if (!isPropertyAvailable(DoorControlModule.ID_DOOR_LOCK, areaId)) {
                Log.d(TAG, "DOOR_LOCK is unavailable.")
                return null
            }
            val value = carPropertyManager.getBooleanProperty(DoorControlModule.ID_DOOR_LOCK, areaId)
            Log.d(TAG, "DOOR_LOCK read as $value")
            value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read DOOR_LOCK - ${e.message}")
            null
        }
    }

    override fun getDoorPosition(areaId: Int): Int? {
        return try {
            if (!isPropertyAvailable(DoorControlModule.ID_DOOR_MOVE, areaId)) {
                Log.d(TAG, "DOOR_MOVE is unavailable.")
                return null
            }
            carPropertyManager.getIntProperty(DoorControlModule.ID_DOOR_MOVE, areaId).also {
                Log.d(TAG, "DOOR_MOVE read as $it")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read DOOR_MOVE - ${e.message}")
            null
        }
    }


    override fun setDoorLock(areaId: Int, lock: Boolean) {
        try {
            Log.e("VehiclePropertyProviderImpl", "🚨 setDoorLock() received lock = $lock")
            if (!isPropertyAvailable(DoorControlModule.ID_DOOR_LOCK, areaId)) {
                Log.e("VehiclePropertyProviderImpl", "❌ DOOR_LOCK property is unavailable!")
                return
            }

            Log.d("VehiclePropertyProviderImpl", "🔥 Writing DOOR_LOCK to $lock for area ID: $areaId")
            carPropertyManager.setBooleanProperty(DoorControlModule.ID_DOOR_LOCK, areaId, lock)

            // Try enforcing the value continuously
            Thread {
                repeat(10) { i ->
                    Thread.sleep(500) // Small delay to let changes register
                    val currentLockState = carPropertyManager.getBooleanProperty(DoorControlModule.ID_DOOR_LOCK, areaId)
                    if (currentLockState != lock) {
                        Log.e("VehiclePropertyProviderImpl", "🚨 DOOR_LOCK overridden at attempt $i! Re-setting to $lock")
                        carPropertyManager.setBooleanProperty(DoorControlModule.ID_DOOR_LOCK, areaId, lock)
                    } else {
                        Log.d("VehiclePropertyProviderImpl", "✅ DOOR_LOCK remains at $lock")
                    }
                }
            }.start()

        } catch (e: Exception) {
            Log.e("VehiclePropertyProviderImpl", "Failed to set DOOR_LOCK - ${e.message}")
        }
    }






    override fun setDoorMove(areaId: Int, value: Int) {
        try {
            if (!isPropertyAvailable(DoorControlModule.ID_DOOR_MOVE, areaId)) {
                Log.d(TAG, "Cannot write DOOR_MOVE, property is unavailable.")
                return
            }
            carPropertyManager.setIntProperty(DoorControlModule.ID_DOOR_MOVE, areaId, value)
            Log.d(TAG, "DOOR_MOVE successfully set to $value")

            // ✅ Send broadcast to notify other components
            val intent = Intent("com.MyCarApp.SET_DOOR_MOVE")
            intent.putExtra("door_move_value", value)
            Log.d(TAG, "Broadcasting SET_DOOR_MOVE with value: $value")
            MyCarApp.appContext.sendBroadcast(intent)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to set DOOR_MOVE - ${e.message}")
        }
    }


    fun registerDoorPropertyCallback() {
        doorLockCallback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                Log.e("VehiclePropertyProviderImpl", "🚨 DOOR_LOCK CHANGED! New Value: $value")
                Log.d("VehiclePropertyProviderImpl", "🔍 Property change triggered by: ${Thread.currentThread().stackTrace.joinToString("\n")}")
            }

            override fun onErrorEvent(propertyId: Int, areaId: Int) {
                Log.d(TAG, "Error accessing property: $propertyId")
            }
        }
        carPropertyManager.registerCallback(doorLockCallback!!, DoorControlModule.ID_DOOR_LOCK, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        Log.d(TAG, "Registered callback for DOOR_LOCK")
    }

    fun unregisterDoorPropertyCallback() {
        doorLockCallback?.let {
            carPropertyManager.unregisterCallback(it, DoorControlModule.ID_DOOR_LOCK)
            Log.d(TAG, "Unregistered callback for DOOR_LOCK")
            doorLockCallback = null
        } ?: Log.d(TAG, "No callback to unregister for DOOR_LOCK")
    }

    private fun isPropertyAvailable(propertyId: Int, areaId: Int): Boolean {
        return carPropertyManager.isPropertyAvailable(propertyId, areaId)
    }
}
