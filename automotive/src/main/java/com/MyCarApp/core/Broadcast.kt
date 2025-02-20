package com.MyCarApp.core

import android.car.Car
import android.car.hardware.property.CarPropertyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.MyCarApp.modules.door_control.DoorControlModule

class ModuleBroadcastReceiver : BroadcastReceiver() {
    companion object {
        var lastLockState: Boolean? = null
        var lastMoveValue: Int? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("ModuleBroadcastReceiver", "🔥🔥🔥 BROADCAST RECEIVED: ${intent?.action} 🔥🔥🔥") // Extreme visibility

        // 🚨 Log all extras for debugging
        val extras = intent?.extras
        if (extras == null) {
            Log.e("ModuleBroadcastReceiver", "🚨 Intent extras are NULL!")
        } else {
            for (key in extras.keySet()) {
                Log.d("ModuleBroadcastReceiver", "📢 Extra -> $key: ${extras[key]}")
            }
        }

        // ✅ Ensure vehiclePropertyProvider is initialized
        val integrationClass = IntegrationClass.getInstance()
        if (integrationClass.getVehiclePropertyProvider() == null) {
            Log.e("ModuleBroadcastReceiver", "❌ vehiclePropertyProvider is NULL! Initializing now...")
            val carPropertyManager = Car.createCar(context).getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
            integrationClass.setCarPropertyManager(carPropertyManager)
            Log.d("ModuleBroadcastReceiver", "✅ vehiclePropertyProvider was successfully initialized!")
        }

        val vehiclePropertyProvider = integrationClass.getVehiclePropertyProvider()
        if (vehiclePropertyProvider == null) {
            Log.e("ModuleBroadcastReceiver", "❌ vehiclePropertyProvider is STILL NULL! Aborting.")
            return
        }

        when (intent?.action) {
            "com.MyCarApp.ACTIVATE_MODULE" -> {
                Log.d("ModuleBroadcastReceiver", "Processing ACTIVATE_MODULE...")
                val moduleName = intent.getStringExtra("module_name")
                Log.d("ModuleBroadcastReceiver", "Module Name: $moduleName")

                if (!moduleName.isNullOrEmpty()) {
                    integrationClass.executeModule(moduleName)
                    Log.d("ModuleBroadcastReceiver", "Triggered module: $moduleName")
                } else {
                    Log.e("ModuleBroadcastReceiver", "Module name is null or empty.")
                }
            }

            "com.MyCarApp.SET_DOOR_LOCK" -> {
                // 🚨 Log every extra received for verification
                val extras = intent.extras
                if (extras != null) {
                    for (key in extras.keySet()) {
                        Log.d("ModuleBroadcastReceiver", "📢 Extra -> $key: ${extras[key]}")
                    }
                } else {
                    Log.e("ModuleBroadcastReceiver", "🚨 Intent extras are NULL!")
                }

                // Extract values explicitly
                val lockInt = if (intent.hasExtra("lock_state_int")) intent.getIntExtra("lock_state_int", -1) else null
                val lockBool = if (intent.hasExtra("lock_state_bool")) intent.getBooleanExtra("lock_state_bool", false) else null

                Log.e("ModuleBroadcastReceiver", "🧐 Raw lock_state_int type: ${lockInt?.javaClass?.simpleName}, Value: $lockInt")
                Log.e("ModuleBroadcastReceiver", "🧐 Raw lock_state BOOLEAN type: ${lockBool?.javaClass?.simpleName}, Value: $lockBool")

                // Determine final lock state
                val lockState = when {
                    lockInt != null && lockInt in listOf(0, 1) -> lockInt != 0  // Convert 0 -> false, 1 -> true
                    lockBool != null -> lockBool  // Use boolean directly if available
                    else -> {
                        Log.e("ModuleBroadcastReceiver", "❌ Unexpected lock_state type! Defaulting to false")
                        false
                    }
                }

                Log.e("ModuleBroadcastReceiver", "🚨 Final extracted lock_state = $lockState")

                // ✅ Check if IntegrationClass and vehiclePropertyProvider exist
                val integrationClass = IntegrationClass.getInstance()
                if (integrationClass == null) {
                    Log.e("ModuleBroadcastReceiver", "❌ IntegrationClass instance is NULL!")
                    return
                }

                val vehiclePropertyProvider = integrationClass.getVehiclePropertyProvider()
                if (vehiclePropertyProvider == null) {
                    Log.e("ModuleBroadcastReceiver", "❌ vehiclePropertyProvider is NULL!")
                    return
                }

                Log.d("ModuleBroadcastReceiver", "✅ Calling setDoorLock() with lockState: $lockState...")
                vehiclePropertyProvider.setDoorLock(DoorControlModule.driverDoorAreaId, lockState)
            }


            "com.MyCarApp.SET_DOOR_MOVE" -> {
                val moveValue = intent.getIntExtra("door_move_value", -1)
                Log.e("ModuleBroadcastReceiver", "🚪 Processing SET_DOOR_MOVE: $moveValue")
                integrationClass.executeModule("door_control")
            }

            else -> {
                Log.e("ModuleBroadcastReceiver", "Unknown broadcast action: ${intent?.action}")
            }
        }
    }
}