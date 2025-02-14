package com.MyCarApp.modules.door_control

import com.MyCarApp.core.OutputObject
import com.MyCarApp.core.PropertyResult
import com.MyCarApp.core.VehiclePropertyProvider
import com.MyCarApp.modules.BaseModule

/**
 * DoorControlModule handles door operations using VehiclePropertyProvider.
 * It wraps HAL calls into high-level methods:
 *  - getDoorState(): Retrieves lock status and door position.
 *  - unlockDoor(): Unlocks the driver's door.
 *  - openDoor(): Opens the driver's door and waits for movement to complete.
 *
 * After each action, notifyCompletion() is called.
 *
 * BEFORE TESTING:
 *  - Property IDs are set to: DOOR_LOCK = 371198722, DOOR_MOVE = 373295873.
 *  - The area ID is set to 0 for emulator compatibility.
 *  - Thread.sleep is used to simulate door movement callbacks.
 *
 * AFTER TESTING:
 *  - Replace Thread.sleep with a proper callback using CarPropertyManager.CarPropertyEventCallback.
 *  - Update the manifest with android.car.permission.CONTROL_CAR_DOORS.
 *  - Replace area ID with the appropriate value (e.g. VehicleAreaDoor.ROW_1_LEFT) for a real vehicle.
 *  - Handle vendor-specific configurations.
 *
 * Testing Coverage:
 *  - Write unit tests that mock VehiclePropertyProvider and verify calls to get/set property methods.
 *  - Validate that error cases (null/exception) produce error outputs.
 */
open class DoorControlModule(private val propertyProvider: VehiclePropertyProvider) : BaseModule("door_control") {

    // For testing: use area id 0 for emulator compatibility.
    private val row1Left = 0

    companion object {
        // For testing, use the following property IDs.
        const val ID_DOOR_LOCK = 371198722  // Correct value for DOOR_LOCK
        const val ID_DOOR_MOVE = 373295873  // Correct value for DOOR_MOVE
    }

    override fun execute(input: OutputObject?) {
        println("Door Control Module: Executing...")
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
            notifyCompletion(output)
        }
    }

    /**
     * Retrieves the door state by querying VehiclePropertyProvider.
     * Returns lock status (Boolean) and door position (Int).
     *
     * Adds error handling for null or exception cases using PropertyResult.
     */
    fun getDoorState(): OutputObject {
        println("Door Control Module: Retrieving door state...")
        return try {
            val lockStatus = propertyProvider.getDoorLockStatus(row1Left)
            val doorPosition = propertyProvider.getDoorPosition(row1Left)

            println("Door Control Module: Lock Status = $lockStatus, Door Position = $doorPosition")

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
            notifyCompletion(output)
            output
        } catch (ex: Exception) {
            println("Door Control Module: Error retrieving door state - ${ex.message}")

            val additionalData: Map<String, PropertyResult<Any>> = mapOf(
                "exception" to PropertyResult.Error(ex.message ?: "Unknown error")
            )
            val output = OutputObject(
                moduleId = moduleId,
                result = "Error: Exception retrieving door state",
                status = false,
                additionalData = additionalData
            )
            notifyCompletion(output)
            output
        }
    }


    /**
     * Unlocks the driver's door.
     * Calls setDoorLock(false) on the VehiclePropertyProvider.
     *
     * Includes error handling using PropertyResult to produce an error output if an exception occurs.
     */
    fun unlockDoor() {
        println("Door Control Module: Unlocking door...")
        try {
            propertyProvider.setDoorLock(row1Left, false)
            println("Door Control Module: Door unlocked successfully.")

            val output = OutputObject(
                moduleId = moduleId,
                result = "DoorUnlocked",
                status = true,
                additionalData = null
            )
            notifyCompletion(output)
        } catch (ex: Exception) {
            println("Door Control Module: Failed to unlock door - ${ex.message}")

            val additionalData: Map<String, PropertyResult<Any>> = mapOf(
                "exception" to PropertyResult.Error(ex.message ?: "Unknown error")
            )
            val output = OutputObject(
                moduleId = moduleId,
                result = "Error: Failed to unlock door",
                status = false,
                additionalData = additionalData
            )
            notifyCompletion(output)
        }
    }


    /**
     * Opens the driver's door.
     * Calls setDoorMove(1) on the VehiclePropertyProvider and waits for movement to complete.
     *
     * Includes error handling using PropertyResult to produce an error output if an exception occurs.
     */
    fun openDoor() {
        println("Door Control Module: Opening door...")
        try {
            propertyProvider.setDoorMove(row1Left, 1)
            // Simulate waiting for door movement to complete.
            Thread.sleep(500)
            println("Door Control Module: Door movement complete.")
            val output = OutputObject(
                moduleId = moduleId,
                result = "DoorOpened",
                status = true,
                additionalData = null
            )
            notifyCompletion(output)
        } catch (ex: Exception) {
            val additionalData: Map<String, PropertyResult<Any>> = mapOf(
                "exception" to PropertyResult.Error(ex.message ?: "Unknown error")
            )
            val output = OutputObject(
                moduleId = moduleId,
                result = "Error: Failed to open door",
                status = false,
                additionalData = additionalData
            )
            notifyCompletion(output)
        }
    }
}
