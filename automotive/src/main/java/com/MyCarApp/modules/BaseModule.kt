package com.MyCarApp.modules

import com.MyCarApp.core.IntegrationClass
import com.MyCarApp.core.OutputObject

/**
 * BaseModule is the abstract parent class for all modules.
 * Each module must extend this class and implement the `execute` method to define its logic.
 */
abstract class BaseModule(protected val moduleId: String) {

    /**
     * Executes the module’s logic.
     * Each module must provide an implementation for this method.
     * @param input Optional input data from the previous module.
     */
    abstract fun execute(input: OutputObject?)

    /**
     * Notifies the IntegrationClass that this module has completed execution.
     * @param output The OutputObject containing the result of the module's execution.
     */
    open fun notifyCompletion(output: OutputObject?) {
        requireNotNull(output) { "OutputObject cannot be null" }
        println("BaseModule: Module '$moduleId' completed execution.")
        IntegrationClass.getInstance().notifyModuleCompleted(moduleId, output)
    }
}
