package com.MyCarApp.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ModuleBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("ModuleBroadcastReceiver", "Received broadcast: ${intent?.action}")

        if (intent?.action == "com.MyCarApp.ACTIVATE_MODULE") {
            val moduleName = intent.getStringExtra("module_name")
            Log.d("ModuleBroadcastReceiver", "Module Name: $moduleName")

            if (!moduleName.isNullOrEmpty()) {
                val integrationClass = IntegrationClass.getInstance()
                integrationClass.executeModule(moduleName)
                Log.d("ModuleBroadcastReceiver", "Triggered module: $moduleName")
            } else {
                Log.e("ModuleBroadcastReceiver", "Module name is null or empty.")
            }
        }
    }
}
