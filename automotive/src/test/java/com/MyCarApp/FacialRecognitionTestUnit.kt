package com.MyCarApp.modules.facial_recognition

import com.MyCarApp.core.OutputObject
import com.MyCarApp.core.PropertyResult
import org.junit.Test
import org.mockito.kotlin.*

class FacialRecognitionModuleTest {

    @Test
    fun `test execute prepares correct OutputObject`() {
        // Arrange
        val module = spy(FacialRecognitionModule("test_facial_recognition"))
        // Stub notifyCompletion to prevent actual side effects.
        doNothing().`when`(module).notifyCompletion(any())

        val expectedOutput = OutputObject(
            moduleId = "test_facial_recognition",
            result = "MatchFound",
            status = true,
            additionalData = mapOf("personName" to PropertyResult.Success("John Doe"))
        )

        // Act
        module.execute(null) // Simulate execution with null input

        // Assert: Verify that notifyCompletion was called with an OutputObject matching the expected one.
        verify(module).notifyCompletion(argThat {
            this.moduleId == expectedOutput.moduleId &&
                    this.result == expectedOutput.result &&
                    this.status == expectedOutput.status &&
                    this.additionalData == expectedOutput.additionalData
        })
    }
}
