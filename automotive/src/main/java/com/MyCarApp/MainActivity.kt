package com.MyCarApp

import android.content.ComponentName
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.MyCarApp.core.IntegrationClass
import com.MyCarApp.core.ModuleBroadcastReceiver
import android.car.Car
import android.car.hardware.property.CarPropertyManager
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var integrationClass: IntegrationClass
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null
    private lateinit var moduleBroadcastReceiver: ModuleBroadcastReceiver

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("MainActivity", "Car service connected.")
            car?.getCarManager(Car.PROPERTY_SERVICE)?.let { manager ->
                carPropertyManager = manager as CarPropertyManager
                integrationClass = IntegrationClass.getInstance()
                integrationClass.setCarPropertyManager(carPropertyManager!!)
                integrationClass.initModules()

                // Register door property callback through IntegrationClass
                integrationClass.registerDoorPropertyCallback()

                Log.d("MainActivity", "Car API successfully initialized.")
            } ?: Log.e("MainActivity", "CarPropertyManager is null.")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w("MainActivity", "Car API disconnected.")
            carPropertyManager = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Car API
        car = Car.createCar(this, serviceConnection)
        car?.connect()

        // ✅ Ensure vehiclePropertyProvider is initialized ASAP
        val integrationClass = IntegrationClass.getInstance()
        val carManager = car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager

        if (carManager != null) {
            integrationClass.setCarPropertyManager(carManager)
            Log.d("MainActivity", "✅ vehiclePropertyProvider manually initialized!")
        } else {
            Log.e("MainActivity", "❌ Failed to initialize CarPropertyManager.")
        }

        // Register broadcast receiver for automatic module execution
        moduleBroadcastReceiver = ModuleBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.MyCarApp.ACTIVATE_MODULE")
        intentFilter.addAction("com.MyCarApp.SET_DOOR_LOCK")
        intentFilter.addAction("com.MyCarApp.SET_DOOR_MOVE")

        Log.d("MainActivity", "Registering ModuleBroadcastReceiver with multiple actions...")

        ContextCompat.registerReceiver(
            this,
            moduleBroadcastReceiver,
            intentFilter,
            null,
            null,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                RECEIVER_NOT_EXPORTED
            } else {
                0
            }
        )

        // 🔥 Manually trigger ModuleBroadcastReceiver to check if it's working
        Log.d("MainActivity", "🔥 Manually triggering ModuleBroadcastReceiver...")

        val testIntent = Intent("com.MyCarApp.SET_DOOR_LOCK")
        testIntent.putExtra("lock_state", true)

        ModuleBroadcastReceiver().onReceive(applicationContext, testIntent)

        Log.e("MainActivity", "🔥🔥🔥 Manually triggered ModuleBroadcastReceiver! 🔥🔥🔥")

        // UI buttons removed; door control is now automatically triggered.
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "Unbinding Car service to prevent leaks.")
        car?.disconnect()

        // Unregister broadcast receiver
        unregisterReceiver(moduleBroadcastReceiver)

        // Unregister door property callback
        integrationClass.unregisterDoorPropertyCallback()
    }
}