package com.MyCarApp.core

import com.MyCarApp.core.OutputObject
import com.MyCarApp.core.PropertyResult
import com.MyCarApp.core.VehiclePropertyProvider
import com.MyCarApp.modules.door_control.DoorControlModule
import com.MyCarApp.modules.facial_recognition.FacialRecognitionModule
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class IntegrationClassTest {

    private lateinit var integration: IntegrationClass
    private lateinit var mockVehiclePropertyProvider: VehiclePropertyProvider

    @Before
    fun setup() {
        integration = IntegrationClass.getInstance()
        integration.clearModules() // Ensure a clean state for each test
        // Create a mock VehiclePropertyProvider instead of CarPropertyManager.
        mockVehiclePropertyProvider = mock<VehiclePropertyProvider>()
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
        // Assert that the module's execute method was called without triggering door control.
        verify(facialRecognition).execute(eq(null))
    }

    @Test
    fun `test notifyModuleCompleted triggers next module`() {
        // Arrange
        val facialRecognition = spy(FacialRecognitionModule("facial_recognition"))
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))
        integration.registerModule("facial_recognition", facialRecognition)
        integration.registerModule("door_control", doorControl)
        // Act
        val output = OutputObject(
            moduleId = "facial_recognition",
            result = "MatchFound",
            status = true,
            additionalData = null
        )
        integration.notifyModuleCompleted("facial_recognition", output)
        // Assert: Verify that door control module was triggered with the correct input.
        verify(doorControl).execute(argThat { out -> out.result == "MatchFound" })
    }

    @Test
    fun `test sequential workflow`() {
        // Arrange
        val facialRecognition = spy(FacialRecognitionModule("facial_recognition"))
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))
        integration.registerModule("facial_recognition", facialRecognition)
        integration.registerModule("door_control", doorControl)
        // Act: Execute the facial recognition module which will automatically trigger the workflow chain.
        integration.executeModule("facial_recognition", null)
        // Assert that facial recognition executed and then door control was triggered once.
        verify(facialRecognition).execute(eq(null))
        verify(doorControl, times(1)).execute(argThat { out -> out.result == "MatchFound" })
    }

    // ----- Door Control Specific Tests -----

    @Test
    fun testUnlockDoor_Success() {
        // Arrange: stub setDoorLock on the mock provider.
        doNothing().whenever(mockVehiclePropertyProvider).setDoorLock(eq(0), eq(false))
        // Act
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))
        doorControl.unlockDoor()
        // Assert: Verify that setDoorLock was called with the expected parameters.
        verify(mockVehiclePropertyProvider).setDoorLock(eq(0), eq(false))
    }

    @Test
    fun testUnlockDoor_Exception() {
        // Arrange: simulate an exception when unlocking the door.
        whenever(mockVehiclePropertyProvider.setDoorLock(any(), eq(false)))
            .thenThrow(RuntimeException("Unlock failed"))
        // Act
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))
        doorControl.unlockDoor()
        // Assert: capture the output passed to notifyCompletion.
        argumentCaptor<OutputObject>().apply {
            verify(doorControl, atLeastOnce()).notifyCompletion(capture())
            val output = firstValue
            assert(output.result == "Error: Failed to unlock door")
            assert(!output.status)
            val additionalData = output.additionalData!!
            val exceptionResult = additionalData["exception"]
            assert(exceptionResult is PropertyResult.Error)
            assert((exceptionResult as PropertyResult.Error).message == "Unlock failed")
        }
    }

    @Test
    fun testOpenDoor_Success() {
        // Arrange: stub setDoorMove on the mock provider.
        doNothing().whenever(mockVehiclePropertyProvider).setDoorMove(eq(0), eq(1))
        // Act
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))
        doorControl.openDoor()
        // Assert: Verify that setDoorMove was called with the expected parameters.
        verify(mockVehiclePropertyProvider).setDoorMove(eq(0), eq(1))
    }

    @Test
    fun testOpenDoor_Exception() {
        // Arrange: simulate an exception when opening the door.
        whenever(mockVehiclePropertyProvider.setDoorMove(any(), eq(1)))
            .thenThrow(RuntimeException("Open failed"))
        // Act
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))
        doorControl.openDoor()
        // Assert: capture the output passed to notifyCompletion.
        argumentCaptor<OutputObject>().apply {
            verify(doorControl, atLeastOnce()).notifyCompletion(capture())
            val output = firstValue
            assert(output.result == "Error: Failed to open door")
            assert(!output.status)
            val additionalData = output.additionalData!!
            val exceptionResult = additionalData["exception"]
            assert(exceptionResult is PropertyResult.Error)
            assert((exceptionResult as PropertyResult.Error).message == "Open failed")
        }
    }
}
