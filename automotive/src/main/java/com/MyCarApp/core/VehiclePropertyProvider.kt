package com.MyCarApp.core

/**
 * VehiclePropertyProvider abstracts vehicle property operations.
 * In production, its implementation will interact with system-level APIs
 * (such as CarPropertyManager) to get or set vehicle properties.
 * For testing, you can provide a fake implementation.
 */
interface VehiclePropertyProvider {
    fun getDoorLockStatus(areaId: Int): Boolean?
    fun getDoorPosition(areaId: Int): Int?
    fun setDoorLock(areaId: Int, lock: Boolean)
    fun setDoorMove(areaId: Int, value: Int)
}
