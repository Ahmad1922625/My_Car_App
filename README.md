This project shows a basic implementation of door lock control using Android Automotive’s CarPropertyManager.

It connects to VehiclePropertyIds.DOOR_LOCK, listens for real-time changes, and lets you unlock the door programmatically by setting the property to false. You also get a small function to read the current door lock status whenever you need it.

Everything runs through CarPropertyManager, and you get log messages showing if the lock state changes or if the unlock command was successful.

This is a real working example of how you can control basic vehicle features using Android Automotive vehicle properties.
