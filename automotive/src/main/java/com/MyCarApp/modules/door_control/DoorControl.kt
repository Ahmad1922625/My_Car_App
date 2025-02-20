package com.MyCarApp.modules.door_control

import android.util.Log
import com.MyCarApp.core.OutputObject
import com.MyCarApp.core.PropertyResult
import com.MyCarApp.core.VehiclePropertyProvider
import com.MyCarApp.modules.BaseModule

open class DoorControlModule(private val propertyProvider: VehiclePropertyProvider) : BaseModule("door_control") {

    private val TAG = "DoorControlModule"

    companion object {
        // ✅ Move driverDoorAreaId here so it's accessible statically
        const val driverDoorAreaId = 1

        // Property IDs
        const val ID_DOOR_LOCK = 371198722
        const val ID_DOOR_MOVE = 373295873
    }

    override fun execute(input: OutputObject?) {
        Log.d(TAG, "execute() called with input: $input")
        // Assuming a successful recognition via input "MatchFound"
        if (input?.result == "MatchFound") {
            unlockDoor()
            openDoor()
        } else {
            val output = OutputObject(
                moduleId = moduleId,
                result = "AccessDenied",
                status = true,
                additionalData = null
            )
            Log.d(TAG, "Input did not contain MatchFound. Notifying completion with AccessDenied.")
            notifyCompletion(output)
        }
    }

    fun getDoorState(): OutputObject {
        Log.d(TAG, "Retrieving door state...")
        return try {
            val lockStatus = propertyProvider.getDoorLockStatus(driverDoorAreaId)
            val doorPosition = propertyProvider.getDoorPosition(driverDoorAreaId)

            Log.d(TAG, "Lock Status = $lockStatus, Door Position = $doorPosition")

            val additionalData: Map<String, PropertyResult<Any>> = mapOf(
                "lockStatus" to (lockStatus?.let { PropertyResult.Success(it) }
                    ?: PropertyResult.Error("Lock status not available")),
                "doorPosition" to (doorPosition?.let { PropertyResult.Success(it) }
                    ?: PropertyResult.Error("Door position not available"))
            )
            val output = OutputObject(
                moduleId = moduleId,
                result = "DoorState",
                status = true,
                additionalData = additionalData
            )
            Log.d(TAG, "Notifying completion with DoorState.")
            notifyCompletion(output)
            output
        } catch (ex: Exception) {
            Log.e(TAG, "Error retrieving door state - ${ex.message}")
            val additionalData: Map<String, PropertyResult<Any>> = mapOf(
                "exception" to PropertyResult.Error(ex.message ?: "Unknown error")
            )
            val output = OutputObject(
                moduleId = moduleId,
                result = "Error: Exception retrieving door state",
                status = false,
                additionalData = additionalData
            )
            Log.d(TAG, "Notifying completion with door state error.")
            notifyCompletion(output)
            output
        }
    }

    fun unlockDoor() {
        Log.d(TAG, "unlockDoor() called")
        try {
            propertyProvider.setDoorLock(driverDoorAreaId, false)
            Log.d(TAG, "Door unlocked successfully.")
            val output = OutputObject(
                moduleId = moduleId,
                result = "DoorUnlocked",
                status = true,
                additionalData = null
            )
            Log.d(TAG, "Notifying completion with DoorUnlocked.")
            notifyCompletion(output)
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to unlock door - ${ex.message}")
            val additionalData: Map<String, PropertyResult<Any>> = mapOf(
                "exception" to PropertyResult.Error(ex.message ?: "Unknown error")
            )
            val output = OutputObject(
                moduleId = moduleId,
                result = "Error: Failed to unlock door",
                status = false,
                additionalData = additionalData
            )
            Log.d(TAG, "Notifying completion with door unlock error.")
            notifyCompletion(output)
        }
    }

    fun openDoor() {
        Log.d(TAG, "openDoor() called")
        try {
            propertyProvider.setDoorMove(driverDoorAreaId, 1)
            Log.d(TAG, "Door movement command issued.")
            val output = OutputObject(
                moduleId = moduleId,
                result = "DoorOpened",
                status = true,
                additionalData = null
            )
            Log.d(TAG, "Notifying completion with DoorOpened.")
            notifyCompletion(output)
        } catch (ex: Exception) {
            Log.e(TAG, "Error: Failed to open door - ${ex.message}")
            val additionalData: Map<String, PropertyResult<Any>> = mapOf(
                "exception" to PropertyResult.Error(ex.message ?: "Unknown error")
            )
            val output = OutputObject(
                moduleId = moduleId,
                result = "Error: Failed to open door",
                status = false,
                additionalData = additionalData
            )
            Log.d(TAG, "Notifying completion with door open error.")
            notifyCompletion(output)
        }
    }
}