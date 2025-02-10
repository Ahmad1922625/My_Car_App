package com.MyCarApp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val welcomeText = findViewById<TextView>(R.id.welcome_text)
        val doorControlButton = findViewById<Button>(R.id.door_control_button)
        val facialRecognitionButton = findViewById<Button>(R.id.facial_recognition_button)

        // Use getString() to retrieve the string from resources.
        doorControlButton.setOnClickListener {
            welcomeText.text = getString(R.string.door_control_clicked)
        }

        facialRecognitionButton.setOnClickListener {
            welcomeText.text = getString(R.string.facial_recognition_clicked)
        }
    }
}
