package com.MyCarApp.modules.door_control

import com.MyCarApp.core.OutputObject
import com.MyCarApp.modules.BaseModule

/**
 * DoorControlModule handles door operations based on the result of previous modules.
 */
open class DoorControlModule : BaseModule("door_control") { // Marked as open for mocking
    override fun execute(input: OutputObject?) {
        println("Door Control Module: Executing...")

        // Check the result from the previous module
        val action = if (input?.result == "MatchFound") "OpenDoor" else "DenyAccess"

        println("Door Control Module: Performing action: $action")

        // Prepare the output object
        val output = OutputObject(
            moduleId = "door_control", // Set the moduleId for the output
            result = action,
            status = true
        )

        // Notify IntegrationClass of completion
        notifyCompletion(output)
    }
}
}}}}

