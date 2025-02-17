package com.MyCarApp.core

import android.car.hardware.property.CarPropertyManager
import android.util.Log
import com.MyCarApp.modules.BaseModule
import com.MyCarApp.modules.door_control.DoorControlModule

class IntegrationClass {

    private val moduleRegistry: MutableMap<String, BaseModule> = mutableMapOf()
    private var carPropertyManager: CarPropertyManager? = null
    private var vehiclePropertyProvider: VehiclePropertyProviderImpl? = null

    fun setCarPropertyManager(manager: CarPropertyManager) {
        this.carPropertyManager = manager
        this.vehiclePropertyProvider = VehiclePropertyProviderImpl(manager)
    }

    fun initModules() {
        Log.d("IntegrationClass", "Initializing modules...")
        if (!moduleRegistry.containsKey("door_control") && carPropertyManager != null) {
            // Use the stored vehiclePropertyProvider instance.
            registerModule("door_control", DoorControlModule(vehiclePropertyProvider!!))
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

        // 🔹 Check if this is a door control action
        if (id == "door_control" && input == null) {
            Log.w("IntegrationClass", "Executing door_control without input. Checking broadcasts...")

            // Try fetching values from broadcasts if available
            val lockState = ModuleBroadcastReceiver.lastLockState
            val moveValue = ModuleBroadcastReceiver.lastMoveValue

            if (lockState != null) {
                Log.d("IntegrationClass", "Applying last received DOOR_LOCK state: $lockState")
                vehiclePropertyProvider?.setDoorLock(DoorControlModule.driverDoorAreaId, lockState)
            }

            if (moveValue != null) {
                Log.d("IntegrationClass", "Applying last received DOOR_MOVE value: $moveValue")
                vehiclePropertyProvider?.setDoorMove(DoorControlModule.driverDoorAreaId, moveValue)
            }
        }

        module.execute(input)
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

    fun setVehiclePropertyProvider(provider: VehiclePropertyProvider) {
        if (!moduleRegistry.containsKey("door_control")) {
            registerModule("door_control", DoorControlModule(provider))
        }
    }

    fun registerDoorPropertyCallback() {
        vehiclePropertyProvider?.registerDoorPropertyCallback()
    }

    fun unregisterDoorPropertyCallback() {
        vehiclePropertyProvider?.unregisterDoorPropertyCallback()
    }

    fun getVehiclePropertyProvider(): VehiclePropertyProviderImpl? {
        return vehiclePropertyProvider
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
