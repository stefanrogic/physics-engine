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
        renderEngine.createStars();
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
        
        // If we just enabled tracking and weren't tracking before, note the change
        if (camera.isTrackingEnabled() && !wasTracking) {
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
        // Pass scroll events to the camera for zoom handling
        camera.handleScrollWheel(yoffset);
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
            case "JUPITER":
                targetPosition.set(sceneManager.getJupiter().getPosition());
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
                viewingDistance = 69.6f * 2.5f; // 2.5x SUN RADIUS - closer for better detail
                break;
            case "MERCURY":
                targetPosition.set(sceneManager.getMercury().getPosition());
                viewingDistance = 0.24f * 15.0f; // 15x MERCURY RADIUS - closer for tiny planet
                break;
            case "VENUS":
                targetPosition.set(sceneManager.getVenus().getPosition());
                viewingDistance = 0.605f * 8.0f; // 8x VENUS RADIUS - closer for better detail
                break;
            case "EARTH":
                targetPosition.set(sceneManager.getEarth().getPosition());
                viewingDistance = 0.637f * 8.0f; // 8x EARTH RADIUS - closer for better detail
                break;
            case "MARS":
                targetPosition.set(sceneManager.getMars().getPosition());
                viewingDistance = 0.339f * 12.0f; // 12x MARS RADIUS - closer for smaller planet
                break;
            case "JUPITER":
                targetPosition.set(sceneManager.getJupiter().getPosition());
                viewingDistance = 6.991f * 125.0f; // 125x JUPITER RADIUS - further back for better full planet view
                break;
            default:
                return;
        }
        
        // APPLY ZOOM MULTIPLIER
        viewingDistance *= camera.getTrackingZoomDistance();
        
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
        
        // RENDER STAR FIELD (BACKGROUND)
        renderEngine.renderStars(mvpMatrix);
        
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
