package com.MyCarApp.modules.door_control

import com.MyCarApp.core.OutputObject
import com.MyCarApp.core.PropertyResult
import com.MyCarApp.modules.BaseModule
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.CarPropertyValue

/**
 * DoorControlModule handles door operations using CarPropertyManager.
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
 *  - Write unit tests that mock CarPropertyManager and verify calls to getProperty and setProperty.
 *  - Validate that error cases (null/exception) produce error outputs.
 */
class DoorControlModule(private val carPropertyManager: CarPropertyManager) : BaseModule("door_control") {

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
     * Retrieves the door state by querying CarPropertyManager.
     * Returns lock status (Boolean) and door position (Int).
     *
     * Adds error handling for null or exception cases using PropertyResult.
     */
    fun getDoorState(): OutputObject {
        return try {
            // Retrieve property values.
            val rawLock = carPropertyManager.getProperty(Boolean::class.java, ID_DOOR_LOCK, row1Left)
            val rawDoor = carPropertyManager.getProperty(Int::class.java, ID_DOOR_MOVE, row1Left)

            // Directly extract the value (the casts are unnecessary).
            val lockStatus: Boolean? = rawLock?.value
            val doorPosition: Int? = rawDoor?.value

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
     * Calls setProperty with false for the lock.
     *
     * Includes error handling using PropertyResult to produce an error output if an exception occurs.
     */
    fun unlockDoor() {
        println("Door Control Module: Unlocking door...")
        try {
            carPropertyManager.setProperty(Boolean::class.java, ID_DOOR_LOCK, row1Left, false)
            val output = OutputObject(
                moduleId = moduleId,
                result = "DoorUnlocked",
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
                result = "Error: Failed to unlock door",
                status = false,
                additionalData = additionalData
            )
            notifyCompletion(output)
        }
    }

    /**
     * Opens the driver's door.
     * Issues a command to open and waits for movement to complete.
     *
     * Includes error handling using PropertyResult to produce an error output if an exception occurs.
     */
    fun openDoor() {
        println("Door Control Module: Opening door...")
        try {
            carPropertyManager.setProperty(Int::class.java, ID_DOOR_MOVE, row1Left, 1)
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
