package com.MyCarApp.modules.door_control

import android.util.Log
import android.car.VehiclePropertyIds
import android.car.hardware.property.CarPropertyManager
import com.MyCarApp.core.OutputObject
import com.MyCarApp.core.PropertyResult
import com.MyCarApp.modules.BaseModule

open class DoorControlModule(private val carPropertyManager: CarPropertyManager) : BaseModule("door_control") {

    private val TAG = "DoorControlModule"

    override fun execute(input: OutputObject?): OutputObject {
        Log.d(TAG, "execute() called with input: $input")

        return if (input?.result == "MatchFound") {
            unlockDoor() // ✅ Ensures function always returns an `OutputObject`
        } else {
            OutputObject(
                moduleId = moduleId,
                result = "AccessDenied",
                status = false
            )
        }
    }

    fun getDoorState(areaId: Int = VehicleAreaDoor.ROW_1_LEFT): OutputObject {
        Log.d(TAG, "Retrieving door state for area ID: $areaId")

        return try {
            // ✅ Dynamically fetch lock status and door position
            val lockStatus = carPropertyManager.getBooleanProperty(VehiclePropertyIds.DOOR_LOCK, areaId)
            val doorPosition = carPropertyManager.getIntProperty(VehiclePropertyIds.DOOR_MOVE, areaId)

            Log.d(TAG, "Lock Status = $lockStatus, Door Position = $doorPosition")

            val additionalData: Map<String, PropertyResult<Any>> = mapOf(
                "lockStatus" to PropertyResult.Success(lockStatus),
                "doorPosition" to PropertyResult.Success(doorPosition)
            )

            // ✅ Notify completion and return OutputObject
            OutputObject(
                moduleId = moduleId,
                result = "DoorState",
                status = true,
                additionalData = additionalData
            ).also { notifyCompletion(it) }

        } catch (ex: Exception) {
            Log.e(TAG, "Error retrieving door state for area ID $areaId - ${ex.message}")

            OutputObject(
                moduleId = moduleId,
                result = "Error: Exception retrieving door state",
                status = false,
                additionalData = mapOf(
                    "exception" to PropertyResult.Error(ex.message ?: "Unknown error")
                )
            ).also { notifyCompletion(it) }
        }
    }

    fun unlockDoor(areaId: Int = VehicleAreaDoor.ROW_1_LEFT): OutputObject {
        Log.d(TAG, "unlockDoor() called for area ID: $areaId")

        return try {
            // ✅ Unlock dynamically based on area ID
            carPropertyManager.setBooleanProperty(VehiclePropertyIds.DOOR_LOCK, areaId, false)

            OutputObject(
                moduleId = moduleId,
                result = "DoorUnlocked",
                status = true
            )

        } catch (ex: Exception) {
            Log.e(TAG, "Failed to unlock door for area ID $areaId - ${ex.message}")

            OutputObject(
                moduleId = moduleId,
                result = "Error: Failed to unlock door",
                status = false
            )
        }
    }

    fun openDoor(areaId: Int = VehicleAreaDoor.ROW_1_LEFT): OutputObject {
        Log.d(TAG, "openDoor() called for area ID: $areaId")

        return try {
            // ✅ Dynamically open door based on area ID
            carPropertyManager.setIntProperty(VehiclePropertyIds.DOOR_MOVE, areaId, 1)

            OutputObject(
                moduleId = moduleId,
                result = "DoorOpened",
                status = true
            )

        } catch (ex: Exception) {
            Log.e(TAG, "Failed to open door for area ID $areaId - ${ex.message}")

            OutputObject(
                moduleId = moduleId,
                result = "Error: Failed to open door",
                status = false
            )
        }
    }

    fun registerDoorPropertyCallback(areaId: Int = VehicleAreaDoor.ROW_1_LEFT) {
        // ✅ Register callback dynamically for specific door
        carPropertyManager.registerCallback(carPropertyListener, VehiclePropertyIds.DOOR_LOCK, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        Log.d(TAG, "Registered callback for DOOR_LOCK on area ID: $areaId")
    }

    private val carPropertyListener = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: android.car.hardware.CarPropertyValue<*>) {
            Log.d(TAG, "🚨 Property changed: ${value.propertyId}, New Value: ${value.value}")
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int) {
            Log.d(TAG, "Error accessing property: $propertyId for area ID: $areaId")
        }
    }
    object VehicleAreaDoor {
        const val ROW_1_LEFT = 0x1
        const val ROW_1_RIGHT = 0x4
        const val ROW_2_LEFT = 0x10
        const val ROW_2_RIGHT = 0x40
    }
}
