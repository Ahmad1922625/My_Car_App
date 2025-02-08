package com.MyCarApp.modules.facial_recognition

import com.MyCarApp.core.OutputObject
import com.MyCarApp.modules.facial_recognition.FacialRecognitionModule
import org.junit.Test
import org.mockito.kotlin.*

class FacialRecognitionModuleTest {

    @Test
    fun `test execute prepares correct OutputObject`() {
        // Arrange
        val module = spy(FacialRecognitionModule("test_facial_recognition")) // Spy on the module
        doNothing().`when`(module).notifyCompletion(any()) // Mock notifyCompletion to prevent real logic

        val expectedOutput = OutputObject(
            moduleId = "test_facial_recognition",
            result = "MatchFound",
            status = true,
            additionalData = mapOf("personName" to "John Doe")
        )

        // Act
        module.execute(null) // Simulate execution with null input

        // Assert
        verify(module).notifyCompletion(argThat {
            this.moduleId == expectedOutput.moduleId &&
                    this.result == expectedOutput.result &&
                    this.status == expectedOutput.status &&
                    this.additionalData == expectedOutput.additionalData
        })
    }
}

