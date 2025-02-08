package com.MyCarApp.core

/**
 * A simple data class to store the result of module execution.
 * @param moduleId The ID of the module that produced this output.
 * @param result The execution result (e.g., "MatchFound" or "NoMatch").
 * @param status Indicates success (true) or failure (false).
 * @param additionalData Optional map containing extra data related to the result.
 */
data class OutputObject(
    val moduleId: String,
    val result: String,
    val status: Boolean,
    val additionalData: Map<String, Any>? = null
)

