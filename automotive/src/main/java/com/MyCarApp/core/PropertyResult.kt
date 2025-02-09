package com.MyCarApp.core

sealed class PropertyResult<out T> {
    data class Success<T>(val value: T) : PropertyResult<T>()
    data class Error(val message: String) : PropertyResult<Nothing>()
}
