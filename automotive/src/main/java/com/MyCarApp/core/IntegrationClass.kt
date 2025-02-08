package com.MyCarApp.core

import com.MyCarApp.modules.BaseModule

/**
 * IntegrationClass is responsible for managing module execution workflows.
 * It registers modules, executes them in a defined sequence, and handles callbacks
 * to determine the next module to run.
 */
class IntegrationClass  {

    /**
     * A registry of available modules, where each module is assigned a unique identifier.
     * This allows for dynamic module execution without hardcoding dependencies.
     */
    private val moduleRegistry: MutableMap<String, BaseModule> = mutableMapOf()

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

    fun initModules() {
        println("IntegrationClass: Initializing modules...")
        // Only use the keys since we don't use the module object here.
        for (id in moduleRegistry.keys) {
            println("Initializing module: $id")
        }
        println("IntegrationClass: All modules initialized successfully.")
    }

    /**
     * Registers a new module in the system.
     * @param id A unique identifier for the module.
     * @param module The instance of the module to be registered.
     */
    fun registerModule(id: String, module: BaseModule) {
        if (moduleRegistry.containsKey(id)) {
            println("IntegrationClass: Module with ID '$id' is already registered.")
            return
        }

        // Add the module to the registry
        moduleRegistry[id] = module
        println("IntegrationClass: Registered module: $id")
    }

    /**
     * Executes a module based on its identifier.
     * @param id The unique identifier of the module to be executed.
     * @param input Optional input data passed from the previous module.
     */
    fun executeModule(id: String, input: OutputObject? = null) {
        val module = moduleRegistry[id]

        if (module == null) {
            println("IntegrationClass: No module found with ID '$id'")
            return
        }

        println("IntegrationClass: Executing module: $id")
        module.execute(input)
    }

    /**
     * Handles module completion notifications.
     * This method is called when a module completes its execution.
     * @param id The identifier of the module that completed execution.
     * @param output The output object containing results from the module.
     */
    fun notifyModuleCompleted(id: String, output: OutputObject) {
        println("IntegrationClass: Module '$id' completed with output: $output")

        // Determine the next module to execute based on logic
        when (output.result) {
            "MatchFound" -> executeModule("door_control", output)
            "NoMatch" -> println("IntegrationClass: No match found. Ending workflow.")
            else -> println("IntegrationClass: No further actions defined for this output.")
        }
    }

    /**
     * Singleton pattern to ensure only one instance of IntegrationClass is used.
     */
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

