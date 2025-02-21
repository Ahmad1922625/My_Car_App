package com.MyCarApp.core

import android.car.VehiclePropertyIds
import android.car.hardware.property.CarPropertyManager
import android.util.Log
import com.MyCarApp.modules.BaseModule
import com.MyCarApp.modules.door_control.DoorControlModule

class IntegrationClass {

    private val moduleRegistry: MutableMap<String, BaseModule> = mutableMapOf()
    private var carPropertyManager: CarPropertyManager? = null

    fun setCarPropertyManager(manager: CarPropertyManager) {
        this.carPropertyManager = manager
    }

    fun initModules() {
        Log.d("IntegrationClass", "Initializing modules...")

        if (!moduleRegistry.containsKey("door_control") && carPropertyManager != null) {
            val doorModule = DoorControlModule(carPropertyManager!!)
            registerModule("door_control", doorModule)

            // ✅ Register property callback here
            doorModule.registerDoorPropertyCallback()
        }

        moduleRegistry.keys.forEach { id ->
            Log.d("IntegrationClass", "Module registered: $id")
        }

        if (carPropertyManager == null) {
            Log.w("IntegrationClass", "carPropertyManager is null. Skipping door_control registration.")
        }

        Log.d("IntegrationClass", "All modules initialized successfully.")
    }


    fun registerModule(id: String, module: BaseModule) {
        if (moduleRegistry.containsKey(id)) {
            Log.w("IntegrationClass", "Module with ID '$id' is already registered.")
            return
        }
        moduleRegistry[id] = module
        Log.d("IntegrationClass", "Registered module: $id")
    }

    fun executeModule(id: String, input: OutputObject? = null) {
        val module = moduleRegistry[id]
        if (module == null) {
            Log.e("IntegrationClass", "No module found with ID '$id'")
            return
        }

        Log.d("IntegrationClass", "Executing module: $id with input: $input")

        val output = module.execute(input) // ✅ `execute()` always returns an `OutputObject`

        notifyModuleCompleted(id, output)
    }

    fun notifyModuleCompleted(id: String, output: OutputObject) {
        Log.d("IntegrationClass", "Module '$id' completed with output: $output")
        // Only trigger door control automatically once when a successful match is detected,
        // and only if the output is coming from a module other than door_control.
        if (output.result == "MatchFound" && id != "door_control") {
            Log.d("IntegrationClass", "Triggering door_control due to MatchFound from module '$id'")
            executeModule("door_control", output)
        } else {
            Log.d("IntegrationClass", "No further actions defined for this output.")
        }
    }

    fun registerDoorPropertyCallback() {
        carPropertyManager?.registerCallback(carPropertyListener, VehiclePropertyIds.DOOR_LOCK, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        Log.d("IntegrationClass", "Registered callback for DOOR_LOCK")
    }

    fun unregisterDoorPropertyCallback() {
        carPropertyManager?.unregisterCallback(carPropertyListener, VehiclePropertyIds.DOOR_LOCK)
        Log.d("IntegrationClass", "Unregistered callback for DOOR_LOCK")
    }

    private val carPropertyListener = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: android.car.hardware.CarPropertyValue<*>) {
            Log.d("IntegrationClass", "🚨 Property changed: ${value.propertyId}, New Value: ${value.value}")
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int) {
            Log.d("IntegrationClass", "Error accessing property: $propertyId")
        }
    }

    companion object {
        private var instance: IntegrationClass? = null

        fun getInstance(): IntegrationClass {
            if (instance == null) {
                instance = IntegrationClass()
            }
            return instance!!
        }
    }
}
