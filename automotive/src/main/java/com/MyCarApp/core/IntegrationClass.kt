package com.MyCarApp.core

import android.car.hardware.property.CarPropertyManager
import android.util.Log
import com.MyCarApp.modules.BaseModule
import com.MyCarApp.modules.door_control.DoorControlModule

/**
 * IntegrationClass manages module execution and module registry.
 */
class IntegrationClass {

    // Registry of available modules.
    private val moduleRegistry: MutableMap<String, BaseModule> = mutableMapOf()

    // Optional CarPropertyManager for modules that require system-level access.
    private var carPropertyManager: CarPropertyManager? = null
    private var vehiclePropertyProvider: VehiclePropertyProviderImpl? = null

    /**
     * Sets the CarPropertyManager instance for modules that require it.
     */
    fun setCarPropertyManager(manager: CarPropertyManager) {
        this.carPropertyManager = manager
        this.vehiclePropertyProvider = VehiclePropertyProviderImpl(manager) // Assign the instance
    }

    /**
     * Initializes modules.
     * If a CarPropertyManager has been set and a door control module is not registered,
     * it automatically registers a DoorControlModule using VehiclePropertyProviderImpl.
     */
    fun initModules() {
        Log.d("IntegrationClass", "Initializing modules...")
        if (!moduleRegistry.containsKey("door_control") && carPropertyManager != null) {
            val provider = VehiclePropertyProviderImpl(carPropertyManager!!)
            registerModule("door_control", DoorControlModule(provider))
        }
        moduleRegistry.keys.forEach { id ->
            Log.d("IntegrationClass", "Initializing module: $id")
        }
        if (carPropertyManager == null) {
            Log.w("IntegrationClass", "carPropertyManager is null. Skipping door_control registration.")
        }
        Log.d("IntegrationClass", "All modules initialized successfully.")
    }

    /**
     * Registers a new module.
     * @param id A unique identifier for the module.
     * @param module The instance of the module to register.
     */
    fun registerModule(id: String, module: BaseModule) {
        if (moduleRegistry.containsKey(id)) {
            Log.w("IntegrationClass", "Module with ID '$id' is already registered.")
            return
        }
        moduleRegistry[id] = module
        Log.d("IntegrationClass", "Registered module: $id")
    }

    /**
     * Executes a module by its identifier.
     * @param id The unique identifier of the module to execute.
     * @param input Optional input data passed from the previous module.
     */
    fun executeModule(id: String, input: OutputObject? = null) {
        val module = moduleRegistry[id]
        if (module == null) {
            Log.e("IntegrationClass", "No module found with ID '$id'")
            return
        }
        Log.d("IntegrationClass", "Executing module: $id")
        module.execute(input)
    }

    /**
     * Handles module completion notifications.
     * This is called when a module completes execution.
     * @param id The identifier of the module that completed.
     * @param output The output object containing results from the module.
     */
    fun notifyModuleCompleted(id: String, output: OutputObject) {
        Log.d("IntegrationClass", "Module '$id' completed with output: $output")
        when (output.result) {
            "MatchFound" -> executeModule("door_control", output)
            "NoMatch" -> Log.d("IntegrationClass", "No match found. Ending workflow.")
            else -> Log.d("IntegrationClass", "No further actions defined for this output.")
        }
    }

    /**
     * Optionally sets a VehiclePropertyProvider directly, allowing you to bypass wrapping a CarPropertyManager.
     */
    fun setVehiclePropertyProvider(provider: VehiclePropertyProvider) {
        if (!moduleRegistry.containsKey("door_control")) {
            registerModule("door_control", DoorControlModule(provider))
        }
    }

    /**
     * Registers and unregisters door property callbacks through VehiclePropertyProvider.
     */
    fun registerDoorPropertyCallback() {
        vehiclePropertyProvider?.registerDoorPropertyCallback()
    }

    fun unregisterDoorPropertyCallback() {
        vehiclePropertyProvider?.unregisterDoorPropertyCallback()
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
