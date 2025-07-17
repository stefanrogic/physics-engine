package com.stefanrogic.core.input;

import com.stefanrogic.core.ui.UIManager;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Handles all input processing including mouse and keyboard events
 */
public class InputHandler {
    
    // UI BUTTON COORDINATES - Use shared constants from UIManager
    private static final float BUTTON_X = UIManager.BUTTON_X;
    private static final float BUTTON_Y = UIManager.BUTTON_Y;
    private static final float BUTTON_WIDTH = UIManager.BUTTON_WIDTH;
    private static final float BUTTON_HEIGHT = UIManager.BUTTON_HEIGHT;
    
    // UI BUTTON COORDINATES - PAUSE BUTTON
    private static final float PAUSE_BUTTON_X = UIManager.PAUSE_BUTTON_X;
    private static final float PAUSE_BUTTON_Y = UIManager.PAUSE_BUTTON_Y;
    private static final float PAUSE_BUTTON_WIDTH = UIManager.PAUSE_BUTTON_WIDTH;
    private static final float PAUSE_BUTTON_HEIGHT = UIManager.PAUSE_BUTTON_HEIGHT;
    
    // UI BUTTON COORDINATES - TRACKING BUTTONS (ROW BELOW)
    private static final float TRACK_BUTTON_WIDTH = UIManager.TRACK_BUTTON_WIDTH;
    private static final float TRACK_BUTTON_HEIGHT = UIManager.TRACK_BUTTON_HEIGHT;
    private static final float TRACK_BUTTON_Y = UIManager.TRACK_BUTTON_Y;
    private static final float SUN_BUTTON_X = UIManager.SUN_BUTTON_X;
    private static final float MERCURY_BUTTON_X = UIManager.MERCURY_BUTTON_X;
    private static final float VENUS_BUTTON_X = UIManager.VENUS_BUTTON_X;
    private static final float EARTH_BUTTON_X = UIManager.EARTH_BUTTON_X;
    // MARS SYSTEM BUTTONS (SECOND ROW) - REMOVED INDIVIDUAL MOON BUTTONS TO REDUCE CLUTTER
    private static final float TRACK_BUTTON_Y2 = UIManager.TRACK_BUTTON_Y2;
    private static final float MARS_BUTTON_X = UIManager.MARS_BUTTON_X;
    
    private final long windowHandle;
    private boolean mousePressed = false; // Track mouse press state
    
    public InputHandler(long windowHandle) {
        this.windowHandle = windowHandle;
    }
    
    public boolean isPointInButton(double mouseX, double mouseY) {
        // GET WINDOW SIZE FOR COORDINATE CONVERSION
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, width, height);
        
        // CONVERT GLFW COORDINATES (TOP-LEFT ORIGIN) TO BUTTON COORDINATES (BOTTOM-LEFT ORIGIN)
        double adjustedY = height[0] - mouseY;
        
        // CHECK IF POINT IS INSIDE BUTTON BOUNDS
        return mouseX >= BUTTON_X && mouseX <= BUTTON_X + BUTTON_WIDTH &&
               adjustedY >= BUTTON_Y && adjustedY <= BUTTON_Y + BUTTON_HEIGHT;
    }
    
    public boolean isPointInPauseButton(double mouseX, double mouseY) {
        // GET WINDOW SIZE FOR COORDINATE CONVERSION
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, width, height);
        
        // CONVERT GLFW COORDINATES (TOP-LEFT ORIGIN) TO BUTTON COORDINATES (BOTTOM-LEFT ORIGIN)
        double adjustedY = height[0] - mouseY;
        
        // CHECK IF POINT IS INSIDE PAUSE BUTTON BOUNDS
        return mouseX >= PAUSE_BUTTON_X && mouseX <= PAUSE_BUTTON_X + PAUSE_BUTTON_WIDTH &&
               adjustedY >= PAUSE_BUTTON_Y && adjustedY <= PAUSE_BUTTON_Y + PAUSE_BUTTON_HEIGHT;
    }
    
    public boolean isPointInTrackingButton(double mouseX, double mouseY, String buttonType) {
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, width, height);
        
        // CONVERT GLFW COORDINATES (TOP-LEFT ORIGIN) TO BUTTON COORDINATES (BOTTOM-LEFT ORIGIN)
        double adjustedY = height[0] - mouseY;
        
        // DETERMINE BUTTON X POSITION BASED ON TYPE
        float buttonX;
        float buttonY;
        switch (buttonType) {
            case "SUN":
                buttonX = SUN_BUTTON_X;
                buttonY = TRACK_BUTTON_Y;
                break;
            case "MERCURY":
                buttonX = MERCURY_BUTTON_X;
                buttonY = TRACK_BUTTON_Y;
                break;
            case "VENUS":
                buttonX = VENUS_BUTTON_X;
                buttonY = TRACK_BUTTON_Y;
                break;
            case "EARTH":
                buttonX = EARTH_BUTTON_X;
                buttonY = TRACK_BUTTON_Y;
                break;
            case "MARS":
                buttonX = MARS_BUTTON_X;
                buttonY = TRACK_BUTTON_Y2;
                break;
            default:
                return false;
        }
        
        // CHECK IF POINT IS INSIDE TRACKING BUTTONS BOUNDS
        return mouseX >= buttonX && mouseX <= buttonX + TRACK_BUTTON_WIDTH &&
               adjustedY >= buttonY && adjustedY <= buttonY + TRACK_BUTTON_HEIGHT;
    }
    
    /**
     * Interface for handling input events
     */
    public interface InputEventHandler {
        void onGridToggle();
        void onPauseToggle();
        void onTrackingChange(String objectName);
        void onCameraMousePress(double x, double y);
        void onCameraMouseRelease();
        void onCameraMouseMove(double x, double y);
        void onCameraScroll(double yoffset);
        void onKeyPress(int key, boolean pressed);
    }
    
    public void setupCallbacks(InputEventHandler handler) {
        // KEYBOARD SHORTCUTS AND MOVEMENT
        glfwSetKeyCallback(windowHandle, (window, key, _, action, _) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true);
            }
            if (key == GLFW_KEY_G && action == GLFW_PRESS) {
                handler.onGridToggle();
            }
            
            // MOVEMENT KEYS - Handle both press and release
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                handler.onKeyPress(key, true);
            } else if (action == GLFW_RELEASE) {
                handler.onKeyPress(key, false);
            }
        });
        
        // MOUSE CALLBACKS FOR CAMERA ROTATION
        glfwSetMouseButtonCallback(windowHandle, (_, button, action, _) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    double[] xpos = new double[1];
                    double[] ypos = new double[1];
                    glfwGetCursorPos(windowHandle, xpos, ypos);
                    
                    // CHECK IF CLICK IS ON GRID BUTTON
                    if (isPointInButton(xpos[0], ypos[0])) {
                        handler.onGridToggle();
                    } 
                    // CHECK IF CLICK IS ON PAUSE BUTTON
                    else if (isPointInPauseButton(xpos[0], ypos[0])) {
                        handler.onPauseToggle();
                    } 
                    // CHECK IF CLICK IS ON TRACKING BUTTONS
                    else if (isPointInTrackingButton(xpos[0], ypos[0], "SUN")) {
                        handler.onTrackingChange("SUN");
                    }
                    else if (isPointInTrackingButton(xpos[0], ypos[0], "MERCURY")) {
                        handler.onTrackingChange("MERCURY");
                    }
                    else if (isPointInTrackingButton(xpos[0], ypos[0], "VENUS")) {
                        handler.onTrackingChange("VENUS");
                    }
                    else if (isPointInTrackingButton(xpos[0], ypos[0], "EARTH")) {
                        handler.onTrackingChange("EARTH");
                    }
                    else if (isPointInTrackingButton(xpos[0], ypos[0], "MARS")) {
                        handler.onTrackingChange("MARS");
                    } else {
                        // START CAMERA ROTATION
                        mousePressed = true;
                        handler.onCameraMousePress(xpos[0], ypos[0]);
                    }
                } else if (action == GLFW_RELEASE) {
                    mousePressed = false;
                    handler.onCameraMouseRelease();
                }
            }
        });
        
        glfwSetCursorPosCallback(windowHandle, (_, xpos, ypos) -> {
            // Only handle mouse movement if mouse is pressed (for camera rotation)
            if (mousePressed) {
                handler.onCameraMouseMove(xpos, ypos);
            }
        });
        
        // SCROLL WHEEL - ZOOM IN/OUT WHEN TRACKING, OTHERWISE UNUSED
        glfwSetScrollCallback(windowHandle, (_, _, yoffset) -> {
            handler.onCameraScroll(yoffset);
        });
    }
}
