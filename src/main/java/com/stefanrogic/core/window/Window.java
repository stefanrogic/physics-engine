package com.stefanrogic.core.window;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.stefanrogic.core.input.InputHandler;
import com.stefanrogic.core.input.Camera;
import com.stefanrogic.core.scene.SceneManager;
import com.stefanrogic.core.rendering.ShaderManager;
import com.stefanrogic.core.rendering.RenderEngine;
import com.stefanrogic.core.rendering.OrbitRenderer;
import com.stefanrogic.core.rendering.ObjectRenderer;
import com.stefanrogic.core.ui.UIManager;

public class Window implements InputHandler.InputEventHandler {
    private final long windowHandle;
    private ShaderManager.ShaderPrograms shaders;
    private final Camera camera;
    private final InputHandler inputHandler;
    private final SceneManager sceneManager;
    
    // NEW COMPONENT ARCHITECTURE  
    private ObjectRenderer objectRenderer;
    private OrbitRenderer orbitRenderer; 
    private UIManager uiManager;
    private RenderEngine renderEngine;
    
    // TRACKING CONTROLS
    private float trackingZoomDistance = 1.0f;
    private boolean preserveCurrentCameraPosition = false;

    public Window(long windowHandle) {
        this.windowHandle = windowHandle;
        this.camera = new Camera();
        this.sceneManager = new SceneManager();
        this.inputHandler = new InputHandler(windowHandle);
    }

    public void create() {
        // Initialize OpenGL capabilities after context is current
        org.lwjgl.opengl.GL.createCapabilities();
        
        // CREATE CELESTIAL OBJECTS FIRST
        sceneManager.createCelestialObjects();
        
        // NOW WE CAN CREATE SHADERS AND COMPONENTS
        this.shaders = new ShaderManager().createShaders();
        
        // INITIALIZE NEW COMPONENTS
        this.objectRenderer = new ObjectRenderer(sceneManager);
        this.orbitRenderer = new OrbitRenderer(sceneManager, shaders);
        this.uiManager = new UIManager(shaders, camera, sceneManager);
        this.renderEngine = new RenderEngine(sceneManager, shaders, camera);
        
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        // INITIALIZE COMPONENTS
        objectRenderer.initializeBuffers();
        renderEngine.createGrid();
        orbitRenderer.createOrbits();
        uiManager.createUI();
        
        // SETUP INPUT CALLBACKS
        inputHandler.setupCallbacks(this);
    }

    // Interface implementations for InputHandler.InputEventHandler
    @Override
    public void onGridToggle() {
        uiManager.setGridVisible(!uiManager.isGridVisible());
    }
    
    @Override
    public void onPauseToggle() {
        sceneManager.setOrbitalMotionPaused(!sceneManager.isOrbitalMotionPaused());
    }
    
    @Override
    public void onTrackingChange(String objectName) {
        // Store current camera state if switching from non-tracking to tracking
        boolean wasTracking = camera.isTrackingEnabled();
        
        camera.setTrackedObject(objectName);
        
        // If we just enabled tracking and weren't tracking before, preserve current camera position
        if (camera.isTrackingEnabled() && !wasTracking) {
            preserveCurrentCameraPosition = true;
            System.out.println("Tracking enabled - will preserve current camera position");
        }
    }
    
    @Override
    public void onCameraMousePress(double x, double y) {
        camera.setMousePressed(true);
        camera.setLastMousePosition(x, y);
    }
    
    @Override
    public void onCameraMouseRelease() {
        camera.setMousePressed(false);
    }
    
    @Override
    public void onCameraMouseMove(double x, double y) {
        camera.handleMouseMovement(x, y);
    }
    
    @Override
    public void onCameraScroll(double yoffset) {
        if (camera.isTrackingEnabled()) {
            trackingZoomDistance *= (1.0f - (float) yoffset * 0.1f);
            trackingZoomDistance = Math.max(0.1f, Math.min(10.0f, trackingZoomDistance));
        }
    }
    
    @Override
    public void onKeyPress(int key, boolean pressed) {
        // Map key presses to camera movement methods (WASD + Shift)
        switch (key) {
            case GLFW_KEY_W -> camera.setWPressed(pressed);
            case GLFW_KEY_S -> camera.setSPressed(pressed);
            case GLFW_KEY_A -> camera.setAPressed(pressed);
            case GLFW_KEY_D -> camera.setDPressed(pressed);
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> camera.setShiftPressed(pressed);
        }
    }

    private void updateCameraMovement() {
        // Check if tracking is enabled and any movement keys are pressed
        boolean anyMovementPressed = camera.getWPressed() || camera.getSPressed() || 
                                   camera.getAPressed() || camera.getDPressed();
        
        if (camera.isTrackingEnabled() && anyMovementPressed) {
            // Get current target position before disabling tracking
            Vector3f targetPosition = getCurrentTrackedTarget();
            if (targetPosition != null) {
                camera.setOrientationToTarget(targetPosition);
            }
        }
        
        // Update camera movement based on key states
        camera.updateMovement();
    }

    private Vector3f getCurrentTrackedTarget() {
        if (!camera.isTrackingEnabled() || "NONE".equals(camera.getTrackedObject())) {
            return null;
        }
        
        Vector3f targetPosition = new Vector3f();
        switch (camera.getTrackedObject()) {
            case "SUN":
                targetPosition.set(0, 0, 0);
                break;
            case "MERCURY":
                targetPosition.set(sceneManager.getMercury().getPosition());
                break;
            case "VENUS":
                targetPosition.set(sceneManager.getVenus().getPosition());
                break;
            case "EARTH":
                targetPosition.set(sceneManager.getEarth().getPosition());
                break;
            case "MARS":
                targetPosition.set(sceneManager.getMars().getPosition());
                break;
            default:
                return null;
        }
        return targetPosition;
    }

    private void updateOrbitalMotion() {
        // Delegate orbital motion updates to SceneManager
        sceneManager.updateOrbitalMotion();
    }

    private void updateCameraTracking() {
        if (!camera.isTrackingEnabled() || "NONE".equals(camera.getTrackedObject())) {
            return;
        }
        
        Vector3f targetPosition = new Vector3f();
        float viewingDistance = 2000.0f; // DEFAULT VIEWING DISTANCE
        
        // GET TARGET POSITION AND APPROPRIATE VIEWING DISTANCE
        // CALCULATE VIEWING DISTANCE BASED ON PLANET RADIUS FOR CONSISTENT APPARENT SIZE
        switch (camera.getTrackedObject()) {
            case "SUN":
                targetPosition.set(0, 0, 0); // SUN IS AT ORIGIN
                viewingDistance = 69.6f * 3.0f; // 3x SUN RADIUS (69.6 units)
                break;
            case "MERCURY":
                targetPosition.set(sceneManager.getMercury().getPosition());
                viewingDistance = 0.24f * 8.0f; // 8x MERCURY RADIUS (0.24 units) - CLOSER FOR TINY PLANET
                break;
            case "VENUS":
                targetPosition.set(sceneManager.getVenus().getPosition());
                viewingDistance = 0.605f * 5.0f; // 5x VENUS RADIUS (0.605 units)
                break;
            case "EARTH":
                targetPosition.set(sceneManager.getEarth().getPosition());
                viewingDistance = 0.637f * 5.0f; // 5x EARTH RADIUS (0.637 units)
                break;
            case "MARS":
                targetPosition.set(sceneManager.getMars().getPosition());
                viewingDistance = 0.339f * 6.0f; // 6x MARS RADIUS (0.339 units) - CLOSER FOR SMALLER PLANET
                break;
            default:
                return;
        }
        
        // IF WE NEED TO PRESERVE CURRENT CAMERA POSITION, CALCULATE DISTANCE AND ANGLES FROM CURRENT POSITION
        if (preserveCurrentCameraPosition) {
            preserveCurrentCameraPosition = false; // Reset flag
            
            // Calculate current distance and angles to target
            float currentDeltaX = camera.getX() - targetPosition.x;
            float currentDeltaY = camera.getY() - targetPosition.y;
            float currentDeltaZ = camera.getZ() - targetPosition.z;
            float currentDistance = (float) Math.sqrt(currentDeltaX * currentDeltaX + currentDeltaY * currentDeltaY + currentDeltaZ * currentDeltaZ);
            
            // Set a reasonable maximum viewing distance (10x the base distance)
            float maxDistance = viewingDistance * 10.0f;
            if (currentDistance > maxDistance) {
                currentDistance = maxDistance;
            }
            
            // Set tracking zoom distance to maintain current distance (but capped)
            trackingZoomDistance = currentDistance / viewingDistance;
            
            System.out.println("Preserved camera position - distance: " + String.format("%.1f", currentDistance) + 
                             ", zoom: " + String.format("%.2f", trackingZoomDistance) + " (max: " + String.format("%.1f", maxDistance) + ")");
            return; // Don't move camera this frame, let it stay where it is
        }
        
        // APPLY ZOOM MULTIPLIER
        viewingDistance *= trackingZoomDistance;
        
        // CALCULATE CAMERA POSITION USING SPHERICAL COORDINATES AROUND TARGET
        // USE CURRENT YAW AND PITCH TO MAINTAIN ORBITAL CAMERA POSITION
        float radPitch = (float) Math.toRadians(camera.getPitch());
        float radYaw = (float) Math.toRadians(camera.getYaw());
        
        float x = targetPosition.x + viewingDistance * (float) (Math.cos(radPitch) * Math.sin(radYaw));
        float y = targetPosition.y + viewingDistance * (float) Math.sin(radPitch);
        float z = targetPosition.z + viewingDistance * (float) (Math.cos(radPitch) * Math.cos(radYaw));
        
        camera.setPosition(x, y, z);
        
        // CAMERA ALWAYS LOOKS AT THE TARGET (NO NEED TO RECALCULATE YAW/PITCH)
        // THE YAW/PITCH VALUES ARE USED FOR POSITIONING, NOT LOOKING DIRECTION
    }

    public void update() {
        // UPDATE CAMERA MOVEMENT BASED ON PRESSED KEYS
        updateCameraMovement();
        updateCameraTracking();
        
        // UPDATE ORBITAL POSITIONS BASED ON TIME
        updateOrbitalMotion();
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, width, height);
        glViewport(0, 0, width[0], height[0]);
        
        // CREATE MATRICES
        Matrix4f mvpMatrix = renderEngine.createMVPMatrix(width[0], height[0]);
        Matrix4f[] projectionAndView = renderEngine.getProjectionAndView(width[0], height[0]);
        Matrix4f projection = projectionAndView[0];
        Matrix4f view = projectionAndView[1];
        
        // RENDER GRID (IF VISIBLE)
        renderEngine.renderGrid(mvpMatrix, uiManager.isGridVisible());
        
        // RENDER ORBITAL PATHS (WITH DISTANCE-BASED VISIBILITY)
        orbitRenderer.renderOrbits(mvpMatrix, camera.getX(), camera.getY(), camera.getZ());
        
        // RENDER CELESTIAL OBJECTS
        renderEngine.renderCelestialObjects(projection, view, width[0], height[0]);
        
        // RENDER UI (2D OVERLAY)
        uiManager.renderUI(width[0], height[0]);
        
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public void destroy() {
        // CLEANUP COMPONENTS
        objectRenderer.cleanup();
        orbitRenderer.cleanup();
        uiManager.cleanup();
        renderEngine.cleanup();
        
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }
    
    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }
}
