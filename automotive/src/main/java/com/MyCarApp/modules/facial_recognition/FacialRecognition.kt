package com.MyCarApp.modules.facial_recognition

import com.MyCarApp.core.OutputObject
import com.MyCarApp.core.PropertyResult
import com.MyCarApp.modules.BaseModule

/**
 * FacialRecognitionModule simulates a facial recognition operation.
 *
 * This module returns an OutputObject with its additionalData values wrapped in PropertyResult,
 * ensuring type consistency with the rest of the application.
 */
open class FacialRecognitionModule(moduleId: String) : BaseModule(moduleId) {

    override fun execute(input: OutputObject?): OutputObject {
        println("Facial Recognition Module: Executing...")

        // Simulated face recognition logic.
        val matchResult = "MatchFound"

        // Prepare the output object.
        val output = OutputObject(
            moduleId = moduleId,
            result = matchResult,
            status = true,
            additionalData = mapOf(
                "personName" to PropertyResult.Success("John Doe")
            )
        )

        println("Facial Recognition Module: OutputObject prepared -> $output")

        // Notify completion before returning
        notifyCompletion(output)

        // ✅ Ensure that `execute()` **returns** an OutputObject
        return output
    }
}
