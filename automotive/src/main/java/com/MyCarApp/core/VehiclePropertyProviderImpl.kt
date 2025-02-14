package com.MyCarApp.core

import android.car.hardware.property.CarPropertyManager
import android.car.hardware.CarPropertyValue
import com.MyCarApp.modules.door_control.DoorControlModule

/**
 * VehiclePropertyProviderImpl interfaces with CarPropertyManager to read/write car properties.
 * - Uses direct `getProperty/setProperty` for polling.
 * - ✅ Now includes `registerDoorPropertyCallback()` to listen for real-time updates.
 * - ✅ Added `isPropertyAvailable()` to check if a property can be accessed.
 * - ✅ Improved logging for debugging.
 */
class VehiclePropertyProviderImpl(private val carPropertyManager: CarPropertyManager) : VehiclePropertyProvider {

    /**
     * Retrieves the door lock status (true/false) or returns null if unavailable.
     */
    override fun getDoorLockStatus(areaId: Int): Boolean? {
        return try {
            if (!isPropertyAvailable(DoorControlModule.ID_DOOR_LOCK, areaId)) {
                println("VehiclePropertyProviderImpl: DOOR_LOCK is unavailable.")
                return null
            }
            val value = carPropertyManager.getBooleanProperty(DoorControlModule.ID_DOOR_LOCK, areaId)
            println("VehiclePropertyProviderImpl: DOOR_LOCK read as $value")
            value
        } catch (e: Exception) {
            println("VehiclePropertyProviderImpl: Failed to read DOOR_LOCK - ${e.message}")
            null
        }
    }

    /**
     * Retrieves the door position (int) or returns null if unavailable.
     */
    override fun getDoorPosition(areaId: Int): Int? {
        return try {
            if (!isPropertyAvailable(DoorControlModule.ID_DOOR_MOVE, areaId)) {
                println("VehiclePropertyProviderImpl: DOOR_MOVE is unavailable.")
                return null
            }
            val value = carPropertyManager.getIntProperty(DoorControlModule.ID_DOOR_MOVE, areaId)
            println("VehiclePropertyProviderImpl: DOOR_MOVE read as $value")
            value
        } catch (e: Exception) {
            println("VehiclePropertyProviderImpl: Failed to read DOOR_MOVE - ${e.message}")
            null
        }
    }

    /**
     * Sets the door lock state (true = locked, false = unlocked).
     */
    override fun setDoorLock(areaId: Int, lock: Boolean) {
        try {
            if (!isPropertyAvailable(DoorControlModule.ID_DOOR_LOCK, areaId)) {
                println("VehiclePropertyProviderImpl: Cannot write DOOR_LOCK, property is unavailable.")
                return
            }
            carPropertyManager.setBooleanProperty(DoorControlModule.ID_DOOR_LOCK, areaId, lock)
            println("VehiclePropertyProviderImpl: DOOR_LOCK successfully set to $lock")
        } catch (e: Exception) {
            println("VehiclePropertyProviderImpl: Failed to set DOOR_LOCK - ${e.message}")
        }
    }

    /**
     * Sets the door movement state (1 = open, 0 = stop, -1 = close).
     */
    override fun setDoorMove(areaId: Int, value: Int) {
        try {
            if (!isPropertyAvailable(DoorControlModule.ID_DOOR_MOVE, areaId)) {
                println("VehiclePropertyProviderImpl: Cannot write DOOR_MOVE, property is unavailable.")
                return
            }
            carPropertyManager.setIntProperty(DoorControlModule.ID_DOOR_MOVE, areaId, value)
            println("VehiclePropertyProviderImpl: DOOR_MOVE successfully set to $value")
        } catch (e: Exception) {
            println("VehiclePropertyProviderImpl: Failed to set DOOR_MOVE - ${e.message}")
        }
    }

    /**
     * ✅ Registers a callback to listen for DOOR_LOCK updates in real-time.
     */
    fun registerDoorPropertyCallback() {
        carPropertyManager.registerCallback(object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                println("VehiclePropertyProviderImpl: DOOR_LOCK changed to ${value.value}")
            }

            override fun onErrorEvent(propertyId: Int, areaId: Int) {
                println("VehiclePropertyProviderImpl: Error accessing property: $propertyId")
            }
        }, DoorControlModule.ID_DOOR_LOCK, CarPropertyManager.SENSOR_RATE_ONCHANGE)

        println("VehiclePropertyProviderImpl: Registered callback for DOOR_LOCK")
    }

    /**
     * ✅ Unregisters the callback to prevent memory leaks.
     */
    fun unregisterDoorPropertyCallback() {
        carPropertyManager.unregisterCallback(object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {}
            override fun onErrorEvent(propertyId: Int, areaId: Int) {}
        })
        println("VehiclePropertyProviderImpl: Unregistered callback for DOOR_LOCK")
    }

    /**
     * ✅ Checks if a car property is available before accessing it.
     */
    private fun isPropertyAvailable(propertyId: Int, areaId: Int): Boolean {
        return carPropertyManager.isPropertyAvailable(propertyId, areaId)
    }
}
