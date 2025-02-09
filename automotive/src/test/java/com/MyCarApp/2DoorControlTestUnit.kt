package com.MyCarApp.modules.door_control

import com.MyCarApp.core.OutputObject
import com.MyCarApp.core.PropertyResult
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.CarPropertyValue

class DoorControlModuleTest {

    private lateinit var carPropertyManager: CarPropertyManager
    private lateinit var doorControlModule: DoorControlModule

    @Before
    fun setUp() {
        // Create a mock CarPropertyManager
        carPropertyManager = mock()
        // Create a spy for our module so we can stub notifyCompletion
        doorControlModule = spy(DoorControlModule(carPropertyManager))
        // Stub notifyCompletion to do nothing, preventing side effects during tests
        doNothing().whenever(doorControlModule).notifyCompletion(any())
    }

    @Test
    fun testGetDoorState_Success() {
        // Arrange: simulate successful retrieval of door properties by returning mocks of CarPropertyValue.
        val lockCPV = mock<CarPropertyValue<Boolean>>()
        whenever(lockCPV.value).thenReturn(true)
        val doorCPV = mock<CarPropertyValue<Int>>()
        whenever(doorCPV.value).thenReturn(10)

        whenever(carPropertyManager.getProperty(Boolean::class.java, DoorControlModule.ID_DOOR_LOCK, any()))
            .thenReturn(lockCPV)
        whenever(carPropertyManager.getProperty(Int::class.java, DoorControlModule.ID_DOOR_MOVE, any()))
            .thenReturn(doorCPV)

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
        whenever(carPropertyManager.getProperty(Boolean::class.java, DoorControlModule.ID_DOOR_LOCK, any()))
            .thenReturn(null)
        whenever(carPropertyManager.getProperty(Int::class.java, DoorControlModule.ID_DOOR_MOVE, any()))
            .thenReturn(null)

        // Act
        val output = doorControlModule.getDoorState()

        // Assert: The module should still return a "DoorState" result with errors in additionalData.
        assert(output.result == "DoorState")
        // In our implementation, status remains true but additionalData indicates errors.
        assert(output.status)
        val additionalData = output.additionalData!!
        val lockResult = additionalData["lockStatus"]
        val doorPosResult = additionalData["doorPosition"]
        assert(lockResult is PropertyResult.Error)
        assert(doorPosResult is PropertyResult.Error)
    }

    @Test
    fun testGetDoorState_Exception() {
        // Arrange: simulate an exception when retrieving a property.
        whenever(carPropertyManager.getProperty<Boolean>(Boolean::class.java, DoorControlModule.ID_DOOR_LOCK, any()))
            .thenThrow(RuntimeException("Test Exception"))

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
        // Arrange: simulate successful unlock operation.
        doNothing().whenever(carPropertyManager)
            .setProperty(Boolean::class.java, DoorControlModule.ID_DOOR_LOCK, any(), eq(false))

        // Act
        doorControlModule.unlockDoor()

        // Assert: verify that setProperty was called correctly.
        verify(carPropertyManager).setProperty(Boolean::class.java, DoorControlModule.ID_DOOR_LOCK, any(), eq(false))
    }

    @Test
    fun testUnlockDoor_Exception() {
        // Arrange: simulate an exception when unlocking the door.
        whenever(carPropertyManager.setProperty(Boolean::class.java, DoorControlModule.ID_DOOR_LOCK, any(), eq(false)))
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
        // Arrange: simulate successful door open operation.
        doNothing().whenever(carPropertyManager)
            .setProperty(Int::class.java, DoorControlModule.ID_DOOR_MOVE, any(), eq(1))

        // Act
        doorControlModule.openDoor()

        // Assert: verify that setProperty was called correctly.
        verify(carPropertyManager).setProperty(Int::class.java, DoorControlModule.ID_DOOR_MOVE, any(), eq(1))
    }

    @Test
    fun testOpenDoor_Exception() {
        // Arrange: simulate an exception when opening the door.
        whenever(carPropertyManager.setProperty(Int::class.java, DoorControlModule.ID_DOOR_MOVE, any(), eq(1)))
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

    @Test
    fun testExecute_MatchFound() {
        // Arrange: simulate a normal scenario for execute() when input.result == "MatchFound".
        val lockCPV = mock<CarPropertyValue<Boolean>>()
        whenever(lockCPV.value).thenReturn(true)
        val doorCPV = mock<CarPropertyValue<Int>>()
        whenever(doorCPV.value).thenReturn(10)

        whenever(carPropertyManager.getProperty(Boolean::class.java, DoorControlModule.ID_DOOR_LOCK, any()))
            .thenReturn(lockCPV)
        whenever(carPropertyManager.getProperty(Int::class.java, DoorControlModule.ID_DOOR_MOVE, any()))
            .thenReturn(doorCPV)
        doNothing().whenever(carPropertyManager).setProperty(any<Class<Boolean>>(), any(), any(), any<Boolean>())
        doNothing().whenever(carPropertyManager).setProperty(any<Class<Int>>(), any(), any(), any<Int>())
        val input = OutputObject(moduleId = "facial_recognition", result = "MatchFound", status = true)

        // Act: execute should trigger unlockDoor and openDoor.
        doorControlModule.execute(input)

        // Assert: verify that setProperty for unlocking and opening were called.
        verify(carPropertyManager).setProperty(Boolean::class.java, DoorControlModule.ID_DOOR_LOCK, any(), eq(false))
        verify(carPropertyManager).setProperty(Int::class.java, DoorControlModule.ID_DOOR_MOVE, any(), eq(1))
    }

    @Test
    fun testExecute_NotMatchFound() {
        // Arrange: input with result other than "MatchFound"
        val input = OutputObject(moduleId = "facial_recognition", result = "NoMatch", status = true)

        // Act
        doorControlModule.execute(input)

        // Assert: verify that notifyCompletion was called with "AccessDenied" and that unlockDoor/openDoor were not invoked.
        argumentCaptor<OutputObject>().apply {
            verify(doorControlModule).notifyCompletion(capture())
            val output = firstValue
            assert(output.result == "AccessDenied")
            assert(output.status)
        }
        verify(doorControlModule, never()).unlockDoor()
        verify(doorControlModule, never()).openDoor()
    }
}
