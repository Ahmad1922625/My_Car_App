package com.MyCarApp.core

import com.MyCarApp.core.OutputObject
import com.MyCarApp.core.PropertyResult
import com.MyCarApp.core.VehiclePropertyProvider
import com.MyCarApp.modules.door_control.DoorControlModule
import com.MyCarApp.modules.facial_recognition.FacialRecognitionModule
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import android.car.hardware.property.CarPropertyManager

class IntegrationClassTest {

    private lateinit var integration: IntegrationClass
    private lateinit var mockCarPropertyManager: CarPropertyManager
    private lateinit var mockVehiclePropertyProvider: VehiclePropertyProvider

    @Before
    fun setup() {
        // Ensure a fresh instance for each test
        integration = IntegrationClass.getInstance()


        // Mock the CarPropertyManager and VehiclePropertyProvider
        mockCarPropertyManager = mock()
        mockVehiclePropertyProvider = mock()

        // Set the mock CarPropertyManager in IntegrationClass
        integration.setCarPropertyManager(mockCarPropertyManager)
    }

    @Test
    fun `test module registration`() {
        // Arrange
        val facialRecognition = spy(FacialRecognitionModule("facial_recognition"))

        // Act
        integration.registerModule("facial_recognition", facialRecognition)


    }

    @Test
    fun `test module execution without chaining`() {
        // Arrange
        val facialRecognition = spy(FacialRecognitionModule("facial_recognition"))
        integration.registerModule("facial_recognition", facialRecognition)

        // Prevent notifyCompletion from triggering the next module
        doNothing().whenever(facialRecognition).notifyCompletion(any())

        // Act
        integration.executeModule("facial_recognition", null)

        // Assert: Ensure the module executed without triggering door control
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

        // Assert: Verify that door control module was triggered with correct input
        verify(doorControl).execute(argThat { out -> out.result == "MatchFound" })
    }

    @Test
    fun `test sequential workflow`() {
        // Arrange
        val facialRecognition = spy(FacialRecognitionModule("facial_recognition"))
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))

        integration.registerModule("facial_recognition", facialRecognition)
        integration.registerModule("door_control", doorControl)

        // Act: Execute facial recognition module (triggers workflow chain)
        integration.executeModule("facial_recognition", null)

        // Assert: Ensure both modules execute in sequence
        verify(facialRecognition).execute(eq(null))
        verify(doorControl, times(1)).execute(argThat { out -> out.result == "MatchFound" })
    }

    // ----- Door Control Specific Tests -----

    @Test
    fun `test unlockDoor success`() {
        // Arrange
        doNothing().whenever(mockVehiclePropertyProvider).setDoorLock(eq(0), eq(false))

        // Act
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))
        doorControl.unlockDoor()

        // Assert
        verify(mockVehiclePropertyProvider).setDoorLock(eq(0), eq(false))
    }

    @Test
    fun `test unlockDoor exception handling`() {
        // Arrange: Simulate exception when unlocking door
        whenever(mockVehiclePropertyProvider.setDoorLock(any(), eq(false)))
            .thenThrow(RuntimeException("Unlock failed"))

        // Act
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))
        doorControl.unlockDoor()

        // Assert: Capture the output passed to notifyCompletion
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
    fun `test openDoor success`() {
        // Arrange
        doNothing().whenever(mockVehiclePropertyProvider).setDoorMove(eq(0), eq(1))

        // Act
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))
        doorControl.openDoor()

        // Assert
        verify(mockVehiclePropertyProvider).setDoorMove(eq(0), eq(1))
    }

    @Test
    fun `test openDoor exception handling`() {
        // Arrange: Simulate exception when opening the door
        whenever(mockVehiclePropertyProvider.setDoorMove(any(), eq(1)))
            .thenThrow(RuntimeException("Open failed"))

        // Act
        val doorControl = spy(DoorControlModule(mockVehiclePropertyProvider))
        doorControl.openDoor()

        // Assert: Capture the output passed to notifyCompletion
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
