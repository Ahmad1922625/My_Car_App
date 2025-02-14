package com.MyCarApp

import android.content.ComponentName
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.MyCarApp.core.IntegrationClass
import com.MyCarApp.core.ModuleBroadcastReceiver
import com.MyCarApp.core.OutputObject
import android.car.Car
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.CarPropertyValue
import android.os.Build
import androidx.core.content.ContextCompat
import com.MyCarApp.modules.door_control.DoorControlModule

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

                // ✅ Register door lock callback through IntegrationClass
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

        val welcomeText = findViewById<TextView>(R.id.welcome_text)
        val doorControlButton = findViewById<Button>(R.id.door_control_button)
        val facialRecognitionButton = findViewById<Button>(R.id.facial_recognition_button)

        // Initialize Car API
        car = Car.createCar(this, serviceConnection)
        car?.connect()

        // ✅ Register broadcast receiver for module execution
        moduleBroadcastReceiver = ModuleBroadcastReceiver()
        val intentFilter = IntentFilter("com.MyCarApp.ACTIVATE_MODULE")

// Fix: Use an explicit Int flag (0 for older versions)
        val receiverFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RECEIVER_NOT_EXPORTED
        } else {
            0 // Older versions use 0 (no flags)
        }

        ContextCompat.registerReceiver(
            this, // Context
            moduleBroadcastReceiver,
            intentFilter,
            null, // No specific permission needed
            null, // No scheduler
            receiverFlag // ✅ Explicit int flag
        )



        // Set up Door Control button click event
        doorControlButton.setOnClickListener {
            welcomeText.text = getString(R.string.door_control_clicked)
            integrationClass.executeModule("door_control", OutputObject("door_control", "MatchFound", true))
        }

        // Set up Facial Recognition button click event
        facialRecognitionButton.setOnClickListener {
            welcomeText.text = getString(R.string.facial_recognition_clicked)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "Unbinding Car service to prevent leaks.")
        car?.disconnect()

        // ✅ Unregister broadcast receiver
        unregisterReceiver(moduleBroadcastReceiver)

        // ✅ Unregister door property callback
        integrationClass.unregisterDoorPropertyCallback()
    }
}
