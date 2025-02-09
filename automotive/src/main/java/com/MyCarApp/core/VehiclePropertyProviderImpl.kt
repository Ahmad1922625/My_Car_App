package com.MyCarApp.core

import android.car.hardware.property.CarPropertyManager
import com.MyCarApp.modules.door_control.DoorControlModule

/**
 * VehiclePropertyProviderImpl is a production implementation of VehiclePropertyProvider.
 * It wraps the CarPropertyManager and extracts the underlying values from CarPropertyValue objects.
 */
class VehiclePropertyProviderImpl(private val carPropertyManager: CarPropertyManager) : VehiclePropertyProvider {
    override fun getDoorLockStatus(areaId: Int): Boolean? {
        return carPropertyManager.getProperty(Boolean::class.java, DoorControlModule.ID_DOOR_LOCK, areaId)?.value as? Boolean
    }

    override fun getDoorPosition(areaId: Int): Int? {
        return carPropertyManager.getProperty(Int::class.java, DoorControlModule.ID_DOOR_MOVE, areaId)?.value as? Int
    }

    override fun setDoorLock(areaId: Int, lock: Boolean) {
        carPropertyManager.setProperty(Boolean::class.java, DoorControlModule.ID_DOOR_LOCK, areaId, lock)
    }

    override fun setDoorMove(areaId: Int, value: Int) {
        carPropertyManager.setProperty(Int::class.java, DoorControlModule.ID_DOOR_MOVE, areaId, value)
    }
}
