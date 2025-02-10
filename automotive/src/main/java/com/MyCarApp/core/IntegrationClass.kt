package com.MyCarApp.core

import android.car.hardware.property.CarPropertyManager
import com.MyCarApp.modules.BaseModule
import com.MyCarApp.modules.door_control.DoorControlModule

/**
 * IntegrationClass is responsible for managing module execution workflows.
 * It registers modules, executes them in a defined sequence, and handles callbacks
 * to determine the next module to run.
 *
 * For modules like DoorControlModule that require access to system-level car properties,
 * you provide a CarPropertyManager via setCarPropertyManager(), which is then wrapped
 * by VehiclePropertyProviderImpl.
 */
class IntegrationClass {

    // Registry of available modules.
    private val moduleRegistry: MutableMap<String, BaseModule> = mutableMapOf()

    // Optional CarPropertyManager for modules that require system-level access.
    private var carPropertyManager: CarPropertyManager? = null

    /**
     * Returns a copy of the registered modules.
     */
    fun getRegisteredModules(): Map<String, BaseModule> = moduleRegistry.toMap()

    /**
     * Clears the registered modules. (For testing purposes)
     */
    fun clearModules() {
        moduleRegistry.clear()
    }

    /**
     * Sets the CarPropertyManager instance for modules that require it.
     */
    fun setCarPropertyManager(manager: CarPropertyManager) {
        this.carPropertyManager = manager
    }

    /**
     * Initializes modules.
     * If a CarPropertyManager has been set and a door control module is not registered,
     * it automatically registers a DoorControlModule using VehiclePropertyProviderImpl.
     */
    fun initModules() {
        println("IntegrationClass: Initializing modules...")
        if (!moduleRegistry.containsKey("door_control") && carPropertyManager != null) {
            // Wrap the CarPropertyManager in a VehiclePropertyProviderImpl.
            val provider = VehiclePropertyProviderImpl(carPropertyManager!!)
            registerModule("door_control", DoorControlModule(provider))
        }
        for (id in moduleRegistry.keys) {
            println("Initializing module: $id")
        }
        if (carPropertyManager == null) {
            println("IntegrationClass: carPropertyManager is null. Skipping door_control registration.")
        }
        println("IntegrationClass: All modules initialized successfully.")
    }

    /**
     * Registers a new module.
     * @param id A unique identifier for the module.
     * @param module The instance of the module to register.
     */
    fun registerModule(id: String, module: BaseModule) {
        if (moduleRegistry.containsKey(id)) {
            println("IntegrationClass: Module with ID '$id' is already registered.")
            return
        }
        moduleRegistry[id] = module
        println("IntegrationClass: Registered module: $id")
    }

    /**
     * Executes a module by its identifier.
     * @param id The unique identifier of the module to execute.
     * @param input Optional input data passed from the previous module.
     */
    fun executeModule(id: String, input: OutputObject? = null) {
        val module = moduleRegistry[id]
        if (module == null) {
            println("IntegrationClass: No module found with ID '$id'")
            return
        }
        println("IntegrationClass: Registered module with ID '$id'")
        println("IntegrationClass: Executing module: $id")
        module.execute(input)
    }

    /**
     * Handles module completion notifications.
     * This is called when a module completes execution.
     * @param id The identifier of the module that completed.
     * @param output The output object containing results from the module.
     */
    fun notifyModuleCompleted(id: String, output: OutputObject) {
        println("IntegrationClass: Module '$id' completed with output: $output")
        when (output.result) {
            "MatchFound" -> executeModule("door_control", output)
            "NoMatch" -> println("IntegrationClass: No match found. Ending workflow.")
            else -> println("IntegrationClass: No further actions defined for this output.")
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
