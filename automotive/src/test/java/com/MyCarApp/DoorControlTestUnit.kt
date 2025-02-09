package com.MyCarApp.modules.door_control

import com.MyCarApp.core.OutputObject
import com.MyCarApp.core.PropertyResult
import com.MyCarApp.core.VehiclePropertyProvider
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class DoorControlModuleTest {

    private lateinit var vehicleProvider: VehiclePropertyProvider
    private lateinit var doorControlModule: DoorControlModule

    @Before
    fun setUp() {
        // Create a mock VehiclePropertyProvider instead of CarPropertyManager.
        vehicleProvider = mock<VehiclePropertyProvider>()
        // Instantiate the module using the provider.
        doorControlModule = spy(DoorControlModule(vehicleProvider))
        // Stub notifyCompletion to do nothing (avoiding side effects during tests).
        doNothing().whenever(doorControlModule).notifyCompletion(any())
    }

    @Test
    fun testGetDoorState_Success() {
        // Arrange: simulate successful retrieval of door properties.
        whenever(vehicleProvider.getDoorLockStatus(any())).thenReturn(true)
        whenever(vehicleProvider.getDoorPosition(any())).thenReturn(10)

        // Act
        val output = doorControlModule.getDoorState()

        // Assert
        assert(output.result == "DoorState")
        assert(output.status)
        val additionalData = output.additionalData!!
        val lockResult = additionalData["lockStatus"]
        val doorPosResult = additionalData["doorPosition"]

        // Verify that lockResult is a success with value true.
        assert(lockResult is PropertyResult.Success<*> && (lockResult as PropertyResult.Success<*>).value == true)
        // Verify that doorPosResult is a success with value 10.
        assert(doorPosResult is PropertyResult.Success<*> && (doorPosResult as PropertyResult.Success<*>).value == 10)
    }

    @Test
    fun testGetDoorState_NullProperties() {
        // Arrange: simulate that both properties return null.
        whenever(vehicleProvider.getDoorLockStatus(any())).thenReturn(null)
        whenever(vehicleProvider.getDoorPosition(any())).thenReturn(null)

        // Act
        val output = doorControlModule.getDoorState()

        // Assert: Expect that additionalData contains errors for both properties.
        assert(output.result == "DoorState")
        assert(output.status)  // The module returns status true but with errors in additionalData.
        val additionalData = output.additionalData!!
        val lockResult = additionalData["lockStatus"]
        val doorPosResult = additionalData["doorPosition"]
        assert(lockResult is PropertyResult.Error)
        assert(doorPosResult is PropertyResult.Error)
    }

    @Test
    fun testGetDoorState_Exception() {
        // Arrange: simulate an exception when retrieving door lock status.
        whenever(vehicleProvider.getDoorLockStatus(any())).thenThrow(RuntimeException("Test Exception"))

        // Act
        val output = doorControlModule.getDoorState()

        // Assert: The output should indicate an exception.
        assert(output.result == "Error: Exception retrieving door state")
        assert(!output.status)
        val additionalData = output.additionalData!!
        val exceptionResult = additionalData["exception"]
        assert(exceptionResult is PropertyResult.Error)
        assert((exceptionResult as PropertyResult.Error).message == "Test Exception")
    }

    @Test
    fun testUnlockDoor_Success() {
        // Arrange: simulate a successful unlock operation.
        doNothing().whenever(vehicleProvider).setDoorLock(any(), eq(false))

        // Act
        doorControlModule.unlockDoor()

        // Assert: verify that setDoorLock was called with the expected parameters.
        verify(vehicleProvider).setDoorLock(eq(0), eq(false))
    }

    @Test
    fun testUnlockDoor_Exception() {
        // Arrange: simulate an exception when unlocking the door.
        whenever(vehicleProvider.setDoorLock(any(), eq(false)))
            .thenThrow(RuntimeException("Unlock failed"))

        // Act
        doorControlModule.unlockDoor()

        // Assert: capture the output passed to notifyCompletion.
        argumentCaptor<OutputObject>().apply {
            verify(doorControlModule, atLeastOnce()).notifyCompletion(capture())
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
        // Arrange: simulate a successful door open operation.
        doNothing().whenever(vehicleProvider).setDoorMove(any(), eq(1))

        // Act
        doorControlModule.openDoor()

        // Assert: verify that setDoorMove was called with the expected parameters.
        verify(vehicleProvider).setDoorMove(eq(0), eq(1))
    }

    @Test
    fun testOpenDoor_Exception() {
        // Arrange: simulate an exception when opening the door.
        whenever(vehicleProvider.setDoorMove(any(), eq(1)))
            .thenThrow(RuntimeException("Open failed"))

        // Act
        doorControlModule.openDoor()

        // Assert: capture the output passed to notifyCompletion.
        argumentCaptor<OutputObject>().apply {
            verify(doorControlModule, atLeastOnce()).notifyCompletion(capture())
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
