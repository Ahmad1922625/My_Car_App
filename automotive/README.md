##README##

System Flow Overview
Application Startup: MainActivity

The app initializes the Car API (Car.createCar()).
It establishes a connection to CarPropertyManager via IntegrationClass.
Calls integrationClass.initModules(), which:
- Registers the door_control module.
- Calls registerDoorPropertyCallback(), so door lock changes trigger real-time updates. 

Listening for Door Lock Changes (registerDoorPropertyCallback)
VehiclePropertyIds.DOOR_LOCK is used to subscribe to updates.
The system automatically detects door lock state changes via the callback.
If a door lock status update is received, the new value is logged and processed.

Getting Door State (getDoorState()):
Fetches door lock (VehiclePropertyIds.DOOR_LOCK) and door position (VehiclePropertyIds.DOOR_MOVE).
Returns an OutputObject containing:
- lockStatus (locked/unlocked)
- doorPosition (open/closed)

Unlocking a Door (unlockDoor()):
Uses carPropertyManager.setBooleanProperty(VehiclePropertyIds.DOOR_LOCK, areaId, false).
Logs success or failure.

Opening a Door (openDoor())
Uses carPropertyManager.setIntProperty(VehiclePropertyIds.DOOR_MOVE, areaId, 1).
Moves the door to the open position.

Executing a Module (executeModule())
Runs when triggered manually or via a condition (e.g., match found in another module).
Calls unlockDoor() if the input OutputObject result is "MatchFound".
Otherwise, returns "AccessDenied".
Module Completion (notifyModuleCompleted())

Logs the result of module execution.
If "MatchFound", it automatically triggers door_control.

Application Shutdown (onDestroy())
Disconnects Car service.
Unregisters door property callbacks to avoid memory leaks.