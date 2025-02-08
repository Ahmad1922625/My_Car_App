package com.MyCarApp.core

import com.MyCarApp.core.OutputObject
import com.MyCarApp.modules.door_control.DoorControlModule
import com.MyCarApp.modules.facial_recognition.FacialRecognitionModule
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class IntegrationClassTest {

    private lateinit var integration: IntegrationClass

    @Before
    fun setup() {
        integration = IntegrationClass.getInstance()
        integration.clearModules() // Ensure a clean state for each test
    }

    @Test
    fun `test module registration`() {
        // Arrange
        val facialRecognition = spy(FacialRecognitionModule("facial_recognition"))

        // Act
        integration.registerModule("facial_recognition", facialRecognition)

        // Assert using the public accessor
        assert(integration.getRegisteredModules()["facial_recognition"] == facialRecognition)
    }

    @Test
    fun `test module execution without chaining`() {
        // Arrange
        val facialRecognition = spy(FacialRecognitionModule("facial_recognition"))
        integration.registerModule("facial_recognition", facialRecognition)
        // Stub notifyCompletion to prevent the integration chain from executing
        doNothing().whenever(facialRecognition).notifyCompletion(any())

        // Act
        integration.executeModule("facial_recognition", null)

        // Assert that the module's execute method was called without triggering door control
        verify(facialRecognition).execute(null)
    }

    @Test
    fun `test notifyModuleCompleted triggers next module`() {
        // Arrange
        val facialRecognition = spy(FacialRecognitionModule("facial_recognition"))
        val doorControl = spy(DoorControlModule())
        integration.registerModule("facial_recognition", facialRecognition)
        integration.registerModule("door_control", doorControl)

        // Act
        val output = OutputObject(
            moduleId = "facial_recognition",
            result = "MatchFound",
            status = true
        )
        integration.notifyModuleCompleted("facial_recognition", output)

        // Assert that door control module was triggered with the correct input
        verify(doorControl).execute(argThat {
            this.result == "MatchFound"
        })
    }

    @Test
    fun `test sequential workflow`() {
        // Arrange
        val facialRecognition = spy(FacialRecognitionModule("facial_recognition"))
        val doorControl = spy(DoorControlModule())
        integration.registerModule("facial_recognition", facialRecognition)
        integration.registerModule("door_control", doorControl)

        // Act: Execute the facial recognition module which will automatically trigger the workflow chain
        integration.executeModule("facial_recognition", null)

        // Assert that facial recognition was executed and then door control was triggered once
        verify(facialRecognition).execute(null)
        verify(doorControl, times(1)).execute(argThat {
            this.result == "MatchFound"
        })
    }
}
