package com.MyCarApp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.MyCarApp.core.IntegrationClass
import com.MyCarApp.core.OutputObject
import android.car.Car
import android.car.hardware.property.CarPropertyManager

class MainActivity : AppCompatActivity() {

    private lateinit var integrationClass: IntegrationClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val welcomeText = findViewById<TextView>(R.id.welcome_text)
        val doorControlButton = findViewById<Button>(R.id.door_control_button)
        val facialRecognitionButton = findViewById<Button>(R.id.facial_recognition_button)

        // Initialize Car API
        val car = Car.createCar(this)
        val carPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

        // Initialize IntegrationClass and set CarPropertyManager
        integrationClass = IntegrationClass.getInstance()
        integrationClass.setCarPropertyManager(carPropertyManager)
        integrationClass.initModules()

        // Set up Door Control button click
        doorControlButton.setOnClickListener {
            welcomeText.text = getString(R.string.door_control_clicked)
            integrationClass.executeModule("door_control", OutputObject("door_control", "MatchFound", true))
        }

        // Set up Facial Recognition button click
        facialRecognitionButton.setOnClickListener {
            welcomeText.text = getString(R.string.facial_recognition_clicked)
        }
    }
}

