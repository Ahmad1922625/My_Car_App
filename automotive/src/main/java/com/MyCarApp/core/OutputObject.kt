package com.MyCarApp.core

/**
 * OutputObject is a simple data carrier that holds the results of module execution.
 * The additionalData map now uses PropertyResult to explicitly report either a successful value or an error.
 */
data class OutputObject(
    val moduleId: String,
    val result: String,
    val status: Boolean,
    val additionalData: Map<String, PropertyResult<Any>>? = null
)



