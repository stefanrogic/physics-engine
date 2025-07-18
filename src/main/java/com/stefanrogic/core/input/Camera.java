package com.stefanrogic.core.input;

import org.joml.Vector3f;

/**
 * Manages camera state, movement, and tracking functionality
 */
public class Camera {
    // Camera position
    private float x = 0.0f; 
    private float y = 5000.0f; // ELEVATED VIEW TO SEE ORBITAL PLANES BETTER
    private float z = 30000.0f; // FURTHER BACK TO PROPERLY SEE EARTH'S ORBIT
    private float pitch = -20.0f; // LOOKING DOWN MORE TO SEE ORBITAL PLANE
    private float yaw = 180.0f;   // FACING TOWARDS THE SUN (NEGATIVE Z DIRECTION)
    
    // Mouse controls
    private boolean mousePressed = false;
    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    
    // Mouse sensitivity settings
    private static final float MOUSE_SENSITIVITY = 0.3f;
    private static final float TRACKING_MOUSE_SENSITIVITY = 0.1f; // FASTER FOR BETTER ORBITAL CAMERA CONTROL
    
    // Movement controls (WASD only - no vertical movement)
    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean aPressed = false;
    private boolean dPressed = false;
    private boolean shiftPressed = false; // SHIFT FOR SPEED BOOST
    private static final float MOVEMENT_SPEED = 0.2f; // BASE SPEED
    private static final float SPEED_BOOST_MULTIPLIER = 5.0f; // 5X SPEED WHEN SHIFT IS HELD
    
    // Camera tracking
    private boolean trackingEnabled = false;
    private String trackedObject = "NONE"; // "SUN", "MERCURY", "VENUS", "EARTH", "MARS", "JUPITER", "NONE"
    private float trackingZoomDistance = 1.0f; // MULTIPLIER FOR VIEWING DISTANCE
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getPitch() { return pitch; }
    public float getYaw() { return yaw; }
    public boolean isTrackingEnabled() { return trackingEnabled; }
    public String getTrackedObject() { return trackedObject; }
    public float getTrackingZoomDistance() { return trackingZoomDistance; }
    
    // Getters for key states
    public boolean getWPressed() { return wPressed; }
    public boolean getSPressed() { return sPressed; }
    public boolean getAPressed() { return aPressed; }
    public boolean getDPressed() { return dPressed; }
    public boolean getShiftPressed() { return shiftPressed; }
    
    // Setters for position (used by tracking)
    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    // Movement key states (WASD + Shift)
    public void setWPressed(boolean pressed) { this.wPressed = pressed; }
    public void setSPressed(boolean pressed) { this.sPressed = pressed; }
    public void setAPressed(boolean pressed) { this.aPressed = pressed; }
    public void setDPressed(boolean pressed) { this.dPressed = pressed; }
    public void setShiftPressed(boolean pressed) { this.shiftPressed = pressed; }
    
    // Mouse handling
    public void setMousePressed(boolean pressed) { this.mousePressed = pressed; }
    public void setLastMousePosition(double x, double y) {
        this.lastMouseX = x;
        this.lastMouseY = y;
    }
    
    public void handleMouseMovement(double xpos, double ypos) {
        if (mousePressed) {
            double deltaX = xpos - lastMouseX;
            double deltaY = ypos - lastMouseY;
            
            // USE DIFFERENT SENSITIVITY BASED ON TRACKING MODE
            float sensitivity = trackingEnabled ? TRACKING_MOUSE_SENSITIVITY : MOUSE_SENSITIVITY;
            
            yaw -= (float) deltaX * sensitivity; // INVERTED X FOR NATURAL MOUSE LOOK
            pitch -= (float) deltaY * sensitivity; // INVERTED Y FOR NATURAL UP/DOWN (LIKE FPS GAMES)
            
            // CLAMP PITCH TO PREVENT FLIPPING
            pitch = Math.max(-89.0f, Math.min(89.0f, pitch));
            
            lastMouseX = xpos;
            lastMouseY = ypos;
        }
    }
    
    public void handleScrollWheel(double yoffset) {
        if (trackingEnabled) {
            // ADJUST ZOOM DISTANCE WHEN TRACKING - MORE SENSITIVE ZOOM
            trackingZoomDistance -= (float) yoffset * 0.15f;
            trackingZoomDistance = Math.max(0.05f, Math.min(10.0f, trackingZoomDistance)); // MUCH CLOSER ZOOM RANGE
            System.out.println("Tracking zoom: " + String.format("%.2f", trackingZoomDistance));
        }
    }
    
    public void updateMovement() {
        // CHECK IF ANY MOVEMENT KEY IS PRESSED (WASD only)
        boolean anyMovementPressed = wPressed || sPressed || aPressed || dPressed;
        
        // IF TRACKING IS ENABLED AND MOVEMENT KEYS ARE PRESSED, DISABLE TRACKING
        if (trackingEnabled && anyMovementPressed) {
            trackingEnabled = false;
            trackedObject = "NONE";
            System.out.println("Camera tracking disabled - manual movement detected");
        }
        
        // ONLY APPLY MANUAL MOVEMENT IF TRACKING IS DISABLED
        if (!trackingEnabled) {
            // CALCULATE 3D MOVEMENT VECTORS BASED ON CAMERA ROTATION (FULL 3D FLIGHT)
            float radYaw = (float) Math.toRadians(yaw);
            float radPitch = (float) Math.toRadians(pitch);
            
            // FORWARD VECTOR (WHERE CAMERA IS LOOKING - FULL 3D)
            // Include pitch for true 3D movement where W goes exactly where you're looking
            float forwardX = (float) (Math.cos(radPitch) * Math.sin(radYaw));
            float forwardY = (float) Math.sin(radPitch);
            float forwardZ = (float) (Math.cos(radPitch) * Math.cos(radYaw));
            
            // RIGHT VECTOR (PERPENDICULAR TO FORWARD, FOR STRAFE - HORIZONTAL ONLY)
            // Right vector stays on horizontal plane for natural strafe movement
            float rightX = (float) Math.cos(radYaw);
            float rightZ = -(float) Math.sin(radYaw);
            
            // CALCULATE MOVEMENT SPEED (BASE SPEED OR BOOSTED WITH SHIFT)
            float currentSpeed = shiftPressed ? MOVEMENT_SPEED * SPEED_BOOST_MULTIPLIER : MOVEMENT_SPEED;
            
            // APPLY MOVEMENT BASED ON PRESSED KEYS (FULL 3D FLIGHT)
            if (wPressed) { // FORWARD - GO EXACTLY WHERE CAMERA IS LOOKING
                x += forwardX * currentSpeed;
                y += forwardY * currentSpeed;
                z += forwardZ * currentSpeed;
            }
            if (sPressed) { // BACKWARD - GO OPPOSITE TO WHERE CAMERA IS LOOKING
                x -= forwardX * currentSpeed;
                y -= forwardY * currentSpeed;
                z -= forwardZ * currentSpeed;
            }
            if (aPressed) { // STRAFE LEFT
                x += rightX * currentSpeed;
                z += rightZ * currentSpeed;
            }
            if (dPressed) { // STRAFE RIGHT
                x -= rightX * currentSpeed;
                z -= rightZ * currentSpeed;
            }
        }
    }
    
    public void setTrackedObject(String objectName) {
        this.trackedObject = objectName;
        this.trackingEnabled = !"NONE".equals(objectName);
        System.out.println("Camera tracking: " + objectName);
    }
    
    public void updateTracking(Vector3f sunPos, Vector3f mercuryPos, Vector3f venusPos, Vector3f earthPos, Vector3f marsPos, Vector3f jupiterPos) {
        if (!trackingEnabled || "NONE".equals(trackedObject)) {
            return;
        }
        
        Vector3f targetPosition = new Vector3f();
        float viewingDistance = 2000.0f; // DEFAULT VIEWING DISTANCE
        
        // GET TARGET POSITION AND APPROPRIATE VIEWING DISTANCE
        switch (trackedObject) {
            case "SUN":
                targetPosition.set(sunPos);
                viewingDistance = 400.0f; // CLOSER BASE DISTANCE FOR SUN
                break;
            case "MERCURY":
                targetPosition.set(mercuryPos);
                viewingDistance = 150.0f; // CLOSER FOR SMALL PLANET
                break;
            case "VENUS":
                targetPosition.set(venusPos);
                viewingDistance = 200.0f; // CLOSER FOR MEDIUM PLANET
                break;
            case "EARTH":
                targetPosition.set(earthPos);
                viewingDistance = 50.0f; // CLOSER DISTANCE FOR SMALLER REALISTIC EARTH
                break;
            case "MARS":
                targetPosition.set(marsPos);
                viewingDistance = 100.0f; // MEDIUM DISTANCE FOR MARS
                break;
            case "JUPITER":
                targetPosition.set(jupiterPos);
                viewingDistance = 300.0f; // LARGE DISTANCE FOR JUPITER (BIGGEST PLANET)
                break;
            default:
                return;
        }
        
        // APPLY ZOOM MULTIPLIER
        viewingDistance *= trackingZoomDistance;
        
        // CALCULATE CAMERA POSITION USING SPHERICAL COORDINATES AROUND TARGET
        // USE CURRENT YAW AND PITCH TO MAINTAIN ORBITAL CAMERA POSITION
        float radPitch = (float) Math.toRadians(pitch);
        float radYaw = (float) Math.toRadians(yaw);
        
        float newX = targetPosition.x + viewingDistance * (float) (Math.cos(radPitch) * Math.sin(radYaw));
        float newY = targetPosition.y + viewingDistance * (float) Math.sin(radPitch);
        float newZ = targetPosition.z + viewingDistance * (float) (Math.cos(radPitch) * Math.cos(radYaw));
        
        x = newX;
        y = newY;
        z = newZ;
        
        // CAMERA ALWAYS LOOKS AT THE TARGET (NO NEED TO RECALCULATE YAW/PITCH)
        // THE YAW/PITCH VALUES ARE USED FOR POSITIONING, NOT LOOKING DIRECTION
    }
    
    // Method to set camera orientation to look at a target (used when breaking tracking)
    public void setOrientationToTarget(Vector3f targetPosition) {
        // CALCULATE DIRECTION VECTOR FROM CAMERA TO TARGET
        float deltaX = targetPosition.x - x;
        float deltaY = targetPosition.y - y;
        float deltaZ = targetPosition.z - z;
        
        // CALCULATE YAW (HORIZONTAL ROTATION)
        yaw = (float) Math.toDegrees(Math.atan2(deltaX, deltaZ));
        
        // CALCULATE PITCH (VERTICAL ROTATION)
        float horizontalDistance = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        pitch = (float) Math.toDegrees(Math.atan2(deltaY, horizontalDistance));
        
        // CLAMP PITCH TO PREVENT FLIPPING
        pitch = Math.max(-89.0f, Math.min(89.0f, pitch));
        
        System.out.println("Camera direction set to target: pitch=" + 
            String.format("%.1f", pitch) + "°, yaw=" + 
            String.format("%.1f", yaw) + "°");
    }
}
