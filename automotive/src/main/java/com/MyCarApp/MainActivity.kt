package com.MyCarApp

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.MyCarApp.core.IntegrationClass
import android.car.Car
import android.car.hardware.property.CarPropertyManager

class MainActivity : AppCompatActivity() {
    private lateinit var integrationClass: IntegrationClass
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("MainActivity", "Car service connected.")
            car?.getCarManager(Car.PROPERTY_SERVICE)?.let { manager ->
                carPropertyManager = manager as CarPropertyManager
                integrationClass = IntegrationClass.getInstance()
                integrationClass.setCarPropertyManager(carPropertyManager!!)
                integrationClass.initModules()

                // ✅ Replaced direct method calls with centralized provider logic
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

        // ✅ Ensures only one centralized initialization
        car = Car.createCar(this, serviceConnection)
        car?.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "Unbinding Car service to prevent leaks.")
        car?.disconnect()

        // ✅ Unregister the door property callback through IntegrationClass
        integrationClass.unregisterDoorPropertyCallback()
    }
}
