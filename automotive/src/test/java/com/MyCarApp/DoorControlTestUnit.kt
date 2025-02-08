package com.MyCarApp.modules.door_control

import com.MyCarApp.core.OutputObject
import org.junit.Test
import org.mockito.kotlin.*

class DoorControlModuleTest {

    @Test
    fun `test execute handles MatchFound correctly`() {
        // Arrange
        val input = OutputObject(
            moduleId = "facial_recognition",
            result = "MatchFound",
            status = true
        )
        val module = spy(DoorControlModule()) // Spy to track method calls
        // Stub notifyCompletion so the real logic is not executed
        doNothing().whenever(module).notifyCompletion(any())

        // Act
        module.execute(input)

        // Assert
        verify(module).notifyCompletion(argThat {
            this.moduleId == "door_control" &&
                    this.result == "OpenDoor" &&
                    this.status
        })
    }

    @Test
    fun `test execute handles NoMatch correctly`() {
        // Arrange
        val input = OutputObject(
            moduleId = "facial_recognition",
            result = "NoMatch",
            status = true
        )
        val module = spy(DoorControlModule()) // Spy to track method calls
        // Stub notifyCompletion so the real logic is not executed
        doNothing().whenever(module).notifyCompletion(any())

        // Act
        module.execute(input)

        // Assert
        verify(module).notifyCompletion(argThat {
            this.moduleId == "door_control" &&
                    this.result == "DenyAccess" &&
                    this.status
        })
    }
}
