package com.stefanrogic.core;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.stefanrogic.objects.Sun;
import com.stefanrogic.objects.Mercury;
import com.stefanrogic.objects.Venus;
import com.stefanrogic.objects.Earth;

public class Window {
    private long windowHandle;
    private int gridShaderProgram, sunShaderProgram;
    private int gridVAO, gridVBO;
    private int orbitVAO, orbitVBO; // FOR ORBITAL PATHS
    private int venusOrbitVAO, venusOrbitVBO; // FOR VENUS ORBIT
    private int earthOrbitVAO, earthOrbitVBO; // FOR EARTH ORBIT
    private int gridMvpLocation, sunMvpLocation, sunColorLocation;
    
    // CAMERA SETTINGS - FPS STYLE
    private float cameraX = 0.0f;
    private float cameraY = 5000.0f; // ELEVATED VIEW TO SEE ORBITAL PLANES BETTER
    private float cameraZ = 30000.0f; // FURTHER BACK TO PROPERLY SEE EARTH'S ORBIT
    private float cameraPitch = -20.0f; // LOOKING DOWN MORE TO SEE ORBITAL PLANE
    private float cameraYaw = 180.0f;   // FACING TOWARDS THE SUN (NEGATIVE Z DIRECTION)
    
    // MOUSE CONTROLS
    private boolean mousePressed = false;
    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    // MOUSE SENSITIVITY SETTINGS
    private final float MOUSE_SENSITIVITY = 0.3f;
    private final float TRACKING_MOUSE_SENSITIVITY = 0.02f; // MUCH SLOWER FOR ORBITAL CAMERA
    
    // MOVEMENT CONTROLS
    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean aPressed = false;
    private boolean dPressed = false;
    private boolean spacePressed = false;
    private boolean shiftPressed = false;
    private final float MOVEMENT_SPEED = 2.0f; // EVEN SLOWER BASE SPEED FOR PRECISION
    private final float MOVEMENT_SPEED_FAST = 20.0f; // FAST SPEED WITH SHIFT
    
    // GRID TOGGLE
    private boolean gridVisible = true;
    
    // ORBITAL MOTION PAUSE
    private boolean orbitalMotionPaused = false;
    
    // CAMERA TRACKING
    private boolean cameraTrackingEnabled = false;
    private String trackedObject = "NONE"; // "SUN", "MERCURY", "VENUS", "EARTH", "NONE"
    
    // UI ELEMENTS
    private int uiShaderProgram;
    private int uiVAO, uiVBO;
    private int pauseButtonVAO, pauseButtonVBO; // FOR PAUSE BUTTON
    private int textVAO, textVBO; // FOR TEXT RENDERING
    private int pauseIconVAO, pauseIconVBO; // FOR PAUSE ICON RENDERING
    
    // TRACKING BUTTON VAOs
    private int sunButtonVAO, sunButtonVBO;
    private int mercuryButtonVAO, mercuryButtonVBO;
    private int venusButtonVAO, venusButtonVBO;
    private int earthButtonVAO, earthButtonVBO;
    private int uiMvpLocation, uiColorLocation;
    
    // UI BUTTON COORDINATES - GRID BUTTON
    private final float BUTTON_X = 20.0f;
    private final float BUTTON_Y = 20.0f;
    private final float BUTTON_WIDTH = 40.0f;
    private final float BUTTON_HEIGHT = 40.0f;
    
    // UI BUTTON COORDINATES - PAUSE BUTTON
    private final float PAUSE_BUTTON_X = 70.0f; // NEXT TO GRID BUTTON
    private final float PAUSE_BUTTON_Y = 20.0f;
    private final float PAUSE_BUTTON_WIDTH = 40.0f;
    private final float PAUSE_BUTTON_HEIGHT = 40.0f;
    
    // UI BUTTON COORDINATES - TRACKING BUTTONS (ROW BELOW)
    private final float TRACK_BUTTON_WIDTH = 30.0f;
    private final float TRACK_BUTTON_HEIGHT = 25.0f;
    private final float TRACK_BUTTON_Y = 70.0f; // BELOW MAIN BUTTONS
    private final float SUN_BUTTON_X = 20.0f;
    private final float MERCURY_BUTTON_X = 55.0f;
    private final float VENUS_BUTTON_X = 90.0f;
    private final float EARTH_BUTTON_X = 125.0f;
    
    // SUN AND PLANETS
    private Sun sun;
    private Mercury mercury;
    private Venus venus;
    private Earth earth;
    
    // TIME TRACKING FOR ORBITAL MOTION
    private long lastTime;
    private static final float TIME_ACCELERATION = 86400.0f; // 1 SECOND = 1 DAY (SPEED UP FACTOR)
    
    // TRACKING CAMERA ZOOM DISTANCE
    private float trackingZoomDistance = 1.0f; // MULTIPLIER FOR VIEWING DISTANCE
    
    public void create(int width, int height, String title) {
        // INIT
        if(!glfwInit()) 
            throw new RuntimeException("Failed to initialize");

        // CREATE WINDOW
        windowHandle = glfwCreateWindow(width, height, title, 0, 0);
        if(windowHandle == 0) 
            throw new RuntimeException("Failed to create window");

        // CENTER WINDOW
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(windowHandle, 
            (videoMode.width() - width) / 2, 
            (videoMode.height() - height) / 2);
        
        // OPENGL SETUP
        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
        
        // ENABLE DEPTH TESTING AND FACE CULLING
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        
        // BG COLOR - SPACE BLACK
        glClearColor(0.02f, 0.02f, 0.05f, 1.0f);
        
        // INITIALIZE SHADERS AND OBJECTS
        initShaders();
        createGrid();
        createSun();
        createMercury();
        createVenus();
        createEarth();
        createOrbits(); // CREATE ORBITS AFTER PLANETS
        createUI();
        
        // SHOW WINDOW
        glfwShowWindow(windowHandle);
        
        // KEYBOARD SHORTCUTS AND MOVEMENT
        glfwSetKeyCallback(windowHandle, (window, key, _, action, _) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true);
            }
            if (key == GLFW_KEY_G && action == GLFW_PRESS) {
                gridVisible = !gridVisible; // TOGGLE GRID WITH 'G' KEY
                System.out.println("Grid " + (gridVisible ? "ON" : "OFF")); // CONSOLE FEEDBACK
            }
            
            // MOVEMENT KEYS
            if (key == GLFW_KEY_W) {
                wPressed = (action == GLFW_PRESS || action == GLFW_REPEAT);
            }
            if (key == GLFW_KEY_S) {
                sPressed = (action == GLFW_PRESS || action == GLFW_REPEAT);
            }
            if (key == GLFW_KEY_A) {
                aPressed = (action == GLFW_PRESS || action == GLFW_REPEAT);
            }
            if (key == GLFW_KEY_D) {
                dPressed = (action == GLFW_PRESS || action == GLFW_REPEAT);
            }
            if (key == GLFW_KEY_SPACE) {
                spacePressed = (action == GLFW_PRESS || action == GLFW_REPEAT);
            }
            if (key == GLFW_KEY_LEFT_SHIFT) {
                shiftPressed = (action == GLFW_PRESS || action == GLFW_REPEAT);
            }
        });
        
        // MOUSE CALLBACKS FOR CAMERA ROTATION
        glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    double[] xpos = new double[1];
                    double[] ypos = new double[1];
                    glfwGetCursorPos(windowHandle, xpos, ypos);
                    
                    // CHECK IF CLICK IS ON GRID BUTTON
                    if (isPointInButton(xpos[0], ypos[0])) {
                        gridVisible = !gridVisible; // TOGGLE GRID
                        System.out.println("Grid " + (gridVisible ? "ON" : "OFF"));
                    } 
                    // CHECK IF CLICK IS ON PAUSE BUTTON
                    else if (isPointInPauseButton(xpos[0], ypos[0])) {
                        orbitalMotionPaused = !orbitalMotionPaused; // TOGGLE PAUSE
                        System.out.println("Orbital Motion " + (orbitalMotionPaused ? "PAUSED" : "RESUMED"));
                        // RESET TIME TRACKING WHEN RESUMING TO AVOID JUMPS
                        if (!orbitalMotionPaused) {
                            lastTime = System.nanoTime();
                        }
                    } 
                    // CHECK IF CLICK IS ON TRACKING BUTTONS
                    else if (isPointInTrackingButton(xpos[0], ypos[0], "SUN")) {
                        setTrackedObject("SUN");
                    }
                    else if (isPointInTrackingButton(xpos[0], ypos[0], "MERCURY")) {
                        setTrackedObject("MERCURY");
                    }
                    else if (isPointInTrackingButton(xpos[0], ypos[0], "VENUS")) {
                        setTrackedObject("VENUS");
                    }
                    else if (isPointInTrackingButton(xpos[0], ypos[0], "EARTH")) {
                        setTrackedObject("EARTH");
                    } else {
                        // START CAMERA ROTATION
                        mousePressed = true;
                        lastMouseX = xpos[0];
                        lastMouseY = ypos[0];
                    }
                } else if (action == GLFW_RELEASE) {
                    mousePressed = false;
                }
            }
        });
        
        glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
            if (mousePressed) {
                double deltaX = xpos - lastMouseX;
                double deltaY = ypos - lastMouseY;
                
                // USE DIFFERENT SENSITIVITY BASED ON TRACKING MODE
                float sensitivity = cameraTrackingEnabled ? TRACKING_MOUSE_SENSITIVITY : MOUSE_SENSITIVITY;
                
                cameraYaw -= (float) deltaX * sensitivity; // INVERTED X FOR NATURAL ROTATION
                cameraPitch += (float) deltaY * sensitivity; // NORMAL Y FOR NATURAL UP/DOWN
                
                // CLAMP PITCH TO PREVENT FLIPPING
                cameraPitch = Math.max(-89.0f, Math.min(89.0f, cameraPitch));
                
                lastMouseX = xpos;
                lastMouseY = ypos;
            }
        });
        
        // SCROLL WHEEL - ZOOM IN/OUT WHEN TRACKING, OTHERWISE UNUSED
        glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) -> {
            if (cameraTrackingEnabled) {
                // ADJUST ZOOM DISTANCE WHEN TRACKING - MORE SENSITIVE ZOOM
                trackingZoomDistance -= (float) yoffset * 0.15f;
                trackingZoomDistance = Math.max(0.05f, Math.min(10.0f, trackingZoomDistance)); // MUCH CLOSER ZOOM RANGE
                System.out.println("Tracking zoom: " + String.format("%.2f", trackingZoomDistance));
            }
        });
        
        // INITIALIZE TIME TRACKING FOR ORBITAL MOTION
        lastTime = System.nanoTime();
        
        // DEBUG: PRINT PLANETARY POSITIONS AT STARTUP
        System.out.println("=== PLANETARY POSITIONS ===");
        System.out.println("Sun: " + sun.getPosition().x + ", " + sun.getPosition().y + ", " + sun.getPosition().z);
        System.out.println("Mercury: " + mercury.getPosition().x + ", " + mercury.getPosition().y + ", " + mercury.getPosition().z + " (distance: " + mercury.getDistanceFromSun() + ")");
        System.out.println("Venus: " + venus.getPosition().x + ", " + venus.getPosition().y + ", " + venus.getPosition().z + " (distance: " + venus.getDistanceFromSun() + ")");
        System.out.println("Earth: " + earth.getPosition().x + ", " + earth.getPosition().y + ", " + earth.getPosition().z + " (distance: " + earth.getDistanceFromSun() + ")");
        System.out.println("Camera: " + cameraX + ", " + cameraY + ", " + cameraZ);
        System.out.println("========================");
    }
    
    private void updateCameraMovement() {
        // CHECK IF ANY MOVEMENT KEY IS PRESSED
        boolean anyMovementPressed = wPressed || sPressed || aPressed || dPressed || spacePressed;
        
        // IF TRACKING IS ENABLED AND MOVEMENT KEYS ARE PRESSED, DISABLE TRACKING
        if (cameraTrackingEnabled && anyMovementPressed) {
            cameraTrackingEnabled = false;
            trackedObject = "NONE";
            System.out.println("Camera tracking disabled - manual movement detected");
        }
        
        // ONLY APPLY MANUAL MOVEMENT IF TRACKING IS DISABLED
        if (!cameraTrackingEnabled) {
            // CALCULATE FORWARD/RIGHT VECTORS BASED ON CAMERA ROTATION
            float radPitch = (float) Math.toRadians(cameraPitch);
            float radYaw = (float) Math.toRadians(cameraYaw);
            
            // FORWARD VECTOR (WHERE CAMERA IS LOOKING)
            float forwardX = (float) (Math.cos(radPitch) * Math.sin(radYaw));
            float forwardY = (float) (-Math.sin(radPitch));
            float forwardZ = (float) (Math.cos(radPitch) * Math.cos(radYaw));
            
            // RIGHT VECTOR (PERPENDICULAR TO FORWARD, FOR STRAFE)
            float rightX = (float) Math.sin(radYaw - Math.PI/2);
            float rightZ = (float) Math.cos(radYaw - Math.PI/2);
            
            // DETERMINE MOVEMENT SPEED (FAST IF SHIFT IS HELD, NORMAL OTHERWISE)
            float currentSpeed = shiftPressed ? MOVEMENT_SPEED_FAST : MOVEMENT_SPEED;
            
            // APPLY MOVEMENT BASED ON PRESSED KEYS
            if (wPressed) { // FORWARD
                cameraX += forwardX * currentSpeed;
                cameraY += forwardY * currentSpeed;
                cameraZ += forwardZ * currentSpeed;
            }
            if (sPressed) { // BACKWARD
                cameraX -= forwardX * currentSpeed;
                cameraY -= forwardY * currentSpeed;
                cameraZ -= forwardZ * currentSpeed;
            }
            if (aPressed) { // STRAFE LEFT
                cameraX -= rightX * currentSpeed;
                cameraZ -= rightZ * currentSpeed;
            }
            if (dPressed) { // STRAFE RIGHT
                cameraX += rightX * currentSpeed;
                cameraZ += rightZ * currentSpeed;
            }
            if (spacePressed) { // UP
                cameraY += currentSpeed;
            }
        }
    }

    private void initShaders() {
        // GRID SHADER (SAME AS BEFORE)
        String gridVertexShader = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            uniform mat4 mvpMatrix;
            void main() {
                gl_Position = mvpMatrix * vec4(aPos, 1.0);
            }
            """;
        
        String gridFragmentShader = """
            #version 330 core
            out vec4 FragColor;
            void main() {
                FragColor = vec4(0.3, 0.3, 0.3, 1.0);
            }
            """;
        
        // SUN SHADER - NO LIGHTING, JUST EMISSIVE GLOW
        String sunVertexShader = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            
            uniform mat4 mvpMatrix;
            
            void main() {
                gl_Position = mvpMatrix * vec4(aPos, 1.0);
            }
            """;
        
        String sunFragmentShader = """
            #version 330 core
            out vec4 FragColor;
            
            uniform vec3 sunColor;
            
            void main() {
                // SUN RADIATES LIGHT - UNIFORM BRIGHT COLOR
                FragColor = vec4(sunColor, 1.0);
            }
            """;
        
        // COMPILE GRID SHADERS
        gridShaderProgram = createShaderProgram(gridVertexShader, gridFragmentShader);
        gridMvpLocation = glGetUniformLocation(gridShaderProgram, "mvpMatrix");
        
        // COMPILE SUN SHADERS
        sunShaderProgram = createShaderProgram(sunVertexShader, sunFragmentShader);
        sunMvpLocation = glGetUniformLocation(sunShaderProgram, "mvpMatrix");
        sunColorLocation = glGetUniformLocation(sunShaderProgram, "sunColor");
        
        // COMPILE UI SHADERS (2D OVERLAY)
        String uiVertexShader = """
            #version 330 core
            layout (location = 0) in vec2 aPos;
            uniform mat4 mvpMatrix;
            void main() {
                gl_Position = mvpMatrix * vec4(aPos, 0.0, 1.0);
            }
            """;
        
        String uiFragmentShader = """
            #version 330 core
            out vec4 FragColor;
            uniform vec3 uiColor;
            void main() {
                FragColor = vec4(uiColor, 0.8);
            }
            """;
        
        uiShaderProgram = createShaderProgram(uiVertexShader, uiFragmentShader);
        uiMvpLocation = glGetUniformLocation(uiShaderProgram, "mvpMatrix");
        uiColorLocation = glGetUniformLocation(uiShaderProgram, "uiColor");
    }
    
    private int createShaderProgram(String vertexSource, String fragmentSource) {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);
        
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);
        
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        
        return program;
    }
    
    private void createGrid() {
        // EXPANDED GRID TO COVER EARTH'S ORBIT (EARTH AT ~15,000 UNITS)
        int gridSize = 200; // EVEN LARGER GRID TO COVER EARTH'S ORBIT
        float spacing = 200.0f; // SPACING (2 MILLION KM PER GRID SQUARE)
        int numLines = (gridSize + 1) * 4;
        float[] vertices = new float[numLines * 3];
        
        int index = 0;
        for (int i = 0; i <= gridSize; i++) {
            float z = (i - gridSize / 2.0f) * spacing;
            vertices[index++] = -gridSize / 2.0f * spacing;
            vertices[index++] = 0.0f;
            vertices[index++] = z;
            vertices[index++] = gridSize / 2.0f * spacing;
            vertices[index++] = 0.0f;
            vertices[index++] = z;
        }
        
        for (int i = 0; i <= gridSize; i++) {
            float x = (i - gridSize / 2.0f) * spacing;
            vertices[index++] = x;
            vertices[index++] = 0.0f;
            vertices[index++] = -gridSize / 2.0f * spacing;
            vertices[index++] = x;
            vertices[index++] = 0.0f;
            vertices[index++] = gridSize / 2.0f * spacing;
        }
        
        gridVAO = glGenVertexArrays();
        gridVBO = glGenBuffers();
        
        glBindVertexArray(gridVAO);
        glBindBuffer(GL_ARRAY_BUFFER, gridVBO);
        
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private void createSun() {
        sun = new Sun();
        
        // CREATE VAO, VBO, EBO FOR THE SUN
        int VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
        
        sun.setVAO(VAO);
        sun.setVBO(VBO);
        sun.setEBO(EBO);
        
        glBindVertexArray(VAO);
        
        // UPLOAD VERTEX DATA
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(sun.getSphere().getVertices().length);
        vertexBuffer.put(sun.getSphere().getVertices()).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        // UPLOAD INDEX DATA
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(sun.getSphere().getIndices().length);
        indexBuffer.put(sun.getSphere().getIndices()).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        
        // POSITION ATTRIBUTE (LOCATION = 0) - ONLY POSITION, NO NORMALS NEEDED FOR SUN
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private void createMercury() {
        mercury = new Mercury();
        
        // CREATE VAO, VBO, EBO FOR MERCURY
        int VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
        
        mercury.setVAO(VAO);
        mercury.setVBO(VBO);
        mercury.setEBO(EBO);
        
        glBindVertexArray(VAO);
        
        // UPLOAD VERTEX DATA
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(mercury.getSphere().getVertices().length);
        vertexBuffer.put(mercury.getSphere().getVertices()).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        // UPLOAD INDEX DATA
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(mercury.getSphere().getIndices().length);
        indexBuffer.put(mercury.getSphere().getIndices()).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        
        // POSITION ATTRIBUTE (LOCATION = 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private void createVenus() {
        venus = new Venus();
        
        // CREATE VAO, VBO, EBO FOR VENUS
        int VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
        
        venus.setVAO(VAO);
        venus.setVBO(VBO);
        venus.setEBO(EBO);
        
        glBindVertexArray(VAO);
        
        // UPLOAD VERTEX DATA
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(venus.getSphere().getVertices().length);
        vertexBuffer.put(venus.getSphere().getVertices()).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        // UPLOAD INDEX DATA
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(venus.getSphere().getIndices().length);
        indexBuffer.put(venus.getSphere().getIndices()).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        
        // POSITION ATTRIBUTE (LOCATION = 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private void createEarth() {
        earth = new Earth();
        
        // CREATE VAO, VBO, EBO FOR EARTH
        int VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
        
        earth.setVAO(VAO);
        earth.setVBO(VBO);
        earth.setEBO(EBO);
        
        glBindVertexArray(VAO);
        
        // UPLOAD VERTEX DATA
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(earth.getSphere().getVertices().length);
        vertexBuffer.put(earth.getSphere().getVertices()).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        // UPLOAD INDEX DATA
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(earth.getSphere().getIndices().length);
        indexBuffer.put(earth.getSphere().getIndices()).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        
        // POSITION ATTRIBUTE (LOCATION = 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private void createUI() {
        // CREATE UI BUTTON (2D QUAD)
        float[] buttonVertices = {
            // BUTTON RECTANGLE (X, Y COORDINATES)
            BUTTON_X, BUTTON_Y,                           // BOTTOM LEFT
            BUTTON_X + BUTTON_WIDTH, BUTTON_Y,            // BOTTOM RIGHT
            BUTTON_X + BUTTON_WIDTH, BUTTON_Y + BUTTON_HEIGHT,  // TOP RIGHT
            BUTTON_X, BUTTON_Y + BUTTON_HEIGHT            // TOP LEFT
        };
        
        uiVAO = glGenVertexArrays();
        uiVBO = glGenBuffers();
        
        glBindVertexArray(uiVAO);
        glBindBuffer(GL_ARRAY_BUFFER, uiVBO);
        
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(buttonVertices.length);
        vertexBuffer.put(buttonVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        // POSITION ATTRIBUTE (LOCATION = 0) - 2D COORDINATES
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        
        // CREATE PAUSE BUTTON (2D QUAD)
        float[] pauseButtonVertices = {
            // PAUSE BUTTON RECTANGLE (X, Y COORDINATES)
            PAUSE_BUTTON_X, PAUSE_BUTTON_Y,                           // BOTTOM LEFT
            PAUSE_BUTTON_X + PAUSE_BUTTON_WIDTH, PAUSE_BUTTON_Y,      // BOTTOM RIGHT
            PAUSE_BUTTON_X + PAUSE_BUTTON_WIDTH, PAUSE_BUTTON_Y + PAUSE_BUTTON_HEIGHT,  // TOP RIGHT
            PAUSE_BUTTON_X, PAUSE_BUTTON_Y + PAUSE_BUTTON_HEIGHT      // TOP LEFT
        };
        
        pauseButtonVAO = glGenVertexArrays();
        pauseButtonVBO = glGenBuffers();
        
        glBindVertexArray(pauseButtonVAO);
        glBindBuffer(GL_ARRAY_BUFFER, pauseButtonVBO);
        
        FloatBuffer pauseVertexBuffer = BufferUtils.createFloatBuffer(pauseButtonVertices.length);
        pauseVertexBuffer.put(pauseButtonVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, pauseVertexBuffer, GL_STATIC_DRAW);
        
        // POSITION ATTRIBUTE (LOCATION = 0) - 2D COORDINATES
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        
        // CREATE TEXT GEOMETRY FOR GRID AND PAUSE ICONS
        createTextGeometry();
        
        // CREATE TRACKING BUTTONS
        createTrackingButtons();
    }
    
    private void createTextGeometry() {
        // SIMPLE GRID ICON (3x3 GRID OF SQUARES)
        float iconX = BUTTON_X + 12.0f; // CENTERED IN 40x40 BUTTON
        float iconY = BUTTON_Y + 12.0f;
        float cellSize = 4.0f;
        float gap = 2.0f;
        
        java.util.List<Float> iconVertices = new java.util.ArrayList<>();
        
        // HELPER FUNCTION TO ADD A SMALL SQUARE (AS TWO TRIANGLES)
        java.util.function.BiConsumer<Float, Float> addSquare = (x, y) -> {
            // TRIANGLE 1
            iconVertices.addAll(java.util.Arrays.asList(x, y, x + cellSize, y, x, y + cellSize));
            // TRIANGLE 2  
            iconVertices.addAll(java.util.Arrays.asList(x + cellSize, y, x + cellSize, y + cellSize, x, y + cellSize));
        };
        
        // CREATE 3x3 GRID OF SMALL SQUARES
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                float x = iconX + col * (cellSize + gap);
                float y = iconY + row * (cellSize + gap);
                addSquare.accept(x, y);
            }
        }
        
        // CONVERT TO ARRAY
        float[] iconArray = new float[iconVertices.size()];
        for (int i = 0; i < iconVertices.size(); i++) {
            iconArray[i] = iconVertices.get(i);
        }
        
        textVAO = glGenVertexArrays();
        textVBO = glGenBuffers();
        
        glBindVertexArray(textVAO);
        glBindBuffer(GL_ARRAY_BUFFER, textVBO);
        
        FloatBuffer iconBuffer = BufferUtils.createFloatBuffer(iconArray.length);
        iconBuffer.put(iconArray).flip();
        glBufferData(GL_ARRAY_BUFFER, iconBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        
        // CREATE PAUSE/PLAY ICON
        createPauseIcon();
    }
    
    private void createPauseIcon() {
        float pauseIconX = PAUSE_BUTTON_X + 12.0f; // CENTERED IN 40x40 BUTTON
        float pauseIconY = PAUSE_BUTTON_Y + 10.0f;
        float barWidth = 4.0f;
        float barHeight = 20.0f;
        float barGap = 6.0f;
        
        java.util.List<Float> pauseIconVertices = new java.util.ArrayList<>();
        
        // LEFT PAUSE BAR (AS TWO TRIANGLES)
        float leftX = pauseIconX;
        pauseIconVertices.addAll(java.util.Arrays.asList(
            leftX, pauseIconY, leftX + barWidth, pauseIconY, leftX, pauseIconY + barHeight,
            leftX + barWidth, pauseIconY, leftX + barWidth, pauseIconY + barHeight, leftX, pauseIconY + barHeight
        ));
        
        // RIGHT PAUSE BAR (AS TWO TRIANGLES)
        float rightX = pauseIconX + barWidth + barGap;
        pauseIconVertices.addAll(java.util.Arrays.asList(
            rightX, pauseIconY, rightX + barWidth, pauseIconY, rightX, pauseIconY + barHeight,
            rightX + barWidth, pauseIconY, rightX + barWidth, pauseIconY + barHeight, rightX, pauseIconY + barHeight
        ));
        
        // CONVERT TO ARRAY
        float[] pauseIconArray = new float[pauseIconVertices.size()];
        for (int i = 0; i < pauseIconVertices.size(); i++) {
            pauseIconArray[i] = pauseIconVertices.get(i);
        }
        
        pauseIconVAO = glGenVertexArrays();
        pauseIconVBO = glGenBuffers();
        
        glBindVertexArray(pauseIconVAO);
        glBindBuffer(GL_ARRAY_BUFFER, pauseIconVBO);
        
        FloatBuffer pauseIconBuffer = BufferUtils.createFloatBuffer(pauseIconArray.length);
        pauseIconBuffer.put(pauseIconArray).flip();
        glBufferData(GL_ARRAY_BUFFER, pauseIconBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private void createOrbits() {
        int orbitSegments = 128; // HIGH DETAIL FOR SMOOTH CIRCLES
        
        // CREATE MERCURY ORBIT
        float mercuryDistance = mercury.getDistanceFromSun();
        java.util.List<Float> mercuryOrbitVertices = new java.util.ArrayList<>();
        
        for (int i = 0; i <= orbitSegments; i++) {
            float angle = (float) (2.0 * Math.PI * i / orbitSegments);
            float x = (float) (mercuryDistance * Math.cos(angle));
            float z = (float) (mercuryDistance * Math.sin(angle));
            float y = 0.0f; // ORBIT ON XZ PLANE
            
            mercuryOrbitVertices.add(x);
            mercuryOrbitVertices.add(y);
            mercuryOrbitVertices.add(z);
        }
        
        // CONVERT MERCURY ORBIT TO ARRAY
        float[] mercuryOrbitArray = new float[mercuryOrbitVertices.size()];
        for (int i = 0; i < mercuryOrbitVertices.size(); i++) {
            mercuryOrbitArray[i] = mercuryOrbitVertices.get(i);
        }
        
        orbitVAO = glGenVertexArrays();
        orbitVBO = glGenBuffers();
        
        glBindVertexArray(orbitVAO);
        glBindBuffer(GL_ARRAY_BUFFER, orbitVBO);
        
        FloatBuffer mercuryOrbitBuffer = BufferUtils.createFloatBuffer(mercuryOrbitArray.length);
        mercuryOrbitBuffer.put(mercuryOrbitArray).flip();
        glBufferData(GL_ARRAY_BUFFER, mercuryOrbitBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        
        // CREATE VENUS ORBIT
        float venusDistance = venus.getDistanceFromSun();
        java.util.List<Float> venusOrbitVertices = new java.util.ArrayList<>();
        
        for (int i = 0; i <= orbitSegments; i++) {
            float angle = (float) (2.0 * Math.PI * i / orbitSegments);
            float x = (float) (venusDistance * Math.cos(angle));
            float z = (float) (venusDistance * Math.sin(angle));
            float y = 0.0f; // ORBIT ON XZ PLANE
            
            venusOrbitVertices.add(x);
            venusOrbitVertices.add(y);
            venusOrbitVertices.add(z);
        }
        
        // CONVERT VENUS ORBIT TO ARRAY
        float[] venusOrbitArray = new float[venusOrbitVertices.size()];
        for (int i = 0; i < venusOrbitVertices.size(); i++) {
            venusOrbitArray[i] = venusOrbitVertices.get(i);
        }
        
        venusOrbitVAO = glGenVertexArrays();
        venusOrbitVBO = glGenBuffers();
        
        glBindVertexArray(venusOrbitVAO);
        glBindBuffer(GL_ARRAY_BUFFER, venusOrbitVBO);
        
        FloatBuffer venusOrbitBuffer = BufferUtils.createFloatBuffer(venusOrbitArray.length);
        venusOrbitBuffer.put(venusOrbitArray).flip();
        glBufferData(GL_ARRAY_BUFFER, venusOrbitBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        
        // CREATE EARTH ORBIT
        float earthDistance = earth.getDistanceFromSun();
        java.util.List<Float> earthOrbitVertices = new java.util.ArrayList<>();
        
        for (int i = 0; i <= orbitSegments; i++) {
            float angle = (float) (2.0 * Math.PI * i / orbitSegments);
            float x = (float) (earthDistance * Math.cos(angle));
            float z = (float) (earthDistance * Math.sin(angle));
            float y = 0.0f; // ORBIT ON XZ PLANE
            
            earthOrbitVertices.add(x);
            earthOrbitVertices.add(y);
            earthOrbitVertices.add(z);
        }
        
        // CONVERT EARTH ORBIT TO ARRAY
        float[] earthOrbitArray = new float[earthOrbitVertices.size()];
        for (int i = 0; i < earthOrbitVertices.size(); i++) {
            earthOrbitArray[i] = earthOrbitVertices.get(i);
        }
        
        earthOrbitVAO = glGenVertexArrays();
        earthOrbitVBO = glGenBuffers();
        
        glBindVertexArray(earthOrbitVAO);
        glBindBuffer(GL_ARRAY_BUFFER, earthOrbitVBO);
        
        FloatBuffer earthOrbitBuffer = BufferUtils.createFloatBuffer(earthOrbitArray.length);
        earthOrbitBuffer.put(earthOrbitArray).flip();
        glBufferData(GL_ARRAY_BUFFER, earthOrbitBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private Matrix4f createMVPMatrix(int width, int height) {
        Matrix4f projection = new Matrix4f();
        projection.perspective((float) Math.toRadians(55.0f), (float) width / height, 1.0f, 100000.0f); // WIDER FOV TO SEE MORE OF THE SOLAR SYSTEM
        
        // CALCULATE LOOK-AT TARGET BASED ON CAMERA MODE
        float lookX, lookY, lookZ;
        
        if (cameraTrackingEnabled && !"NONE".equals(trackedObject)) {
            // WHEN TRACKING, ALWAYS LOOK AT THE TRACKED OBJECT
            Vector3f targetPosition = new Vector3f();
            switch (trackedObject) {
                case "SUN":
                    targetPosition.set(0, 0, 0);
                    break;
                case "MERCURY":
                    targetPosition.set(mercury.getPosition());
                    break;
                case "VENUS":
                    targetPosition.set(venus.getPosition());
                    break;
                case "EARTH":
                    targetPosition.set(earth.getPosition());
                    break;
                default:
                    targetPosition.set(0, 0, 0);
            }
            lookX = targetPosition.x;
            lookY = targetPosition.y;
            lookZ = targetPosition.z;
        } else {
            // FREE CAMERA MODE - CALCULATE LOOK-AT TARGET BASED ON CAMERA ROTATION (FPS STYLE)
            float radPitch = (float) Math.toRadians(cameraPitch);
            float radYaw = (float) Math.toRadians(cameraYaw);
            
            lookX = cameraX + (float) (Math.cos(radPitch) * Math.sin(radYaw));
            lookY = cameraY - (float) Math.sin(radPitch);
            lookZ = cameraZ + (float) (Math.cos(radPitch) * Math.cos(radYaw));
        }
        
        Matrix4f view = new Matrix4f();
        view.lookAt(cameraX, cameraY, cameraZ, lookX, lookY, lookZ, 0, 1, 0);
        
        Matrix4f model = new Matrix4f();
        
        Matrix4f mvp = new Matrix4f();
        projection.mul(view, mvp);
        mvp.mul(model);
        
        return mvp;
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }
    
    public void update() {
        // UPDATE CAMERA MOVEMENT BASED ON PRESSED KEYS
        updateCameraMovement();
        updateCameraTracking();
        
        // UPDATE ORBITAL POSITIONS BASED ON TIME
        updateOrbitalMotion();
        
        // UPDATE CAMERA TRACKING IF ENABLED
        updateCameraTracking();
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, width, height);
        glViewport(0, 0, width[0], height[0]);
        
        Matrix4f mvpMatrix = createMVPMatrix(width[0], height[0]);
        
        // RENDER GRID (IF VISIBLE)
        if (gridVisible) {
            glUseProgram(gridShaderProgram);
            FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
            mvpMatrix.get(matrixBuffer);
            glUniformMatrix4fv(gridMvpLocation, false, matrixBuffer);
            glBindVertexArray(gridVAO);
            glDrawArrays(GL_LINES, 0, 804); // 201 * 4 VERTICES (UPDATED FOR EARTH ORBIT COVERAGE)
        }
        
        // RENDER ORBITAL PATHS
        glUseProgram(gridShaderProgram); // USE SAME SHADER AS GRID FOR THIN LINES
        FloatBuffer orbitMatrixBuffer = BufferUtils.createFloatBuffer(16);
        mvpMatrix.get(orbitMatrixBuffer);
        glUniformMatrix4fv(gridMvpLocation, false, orbitMatrixBuffer);
        
        // RENDER MERCURY ORBIT
        glBindVertexArray(orbitVAO);
        glDrawArrays(GL_LINE_LOOP, 0, 129); // 128 SEGMENTS + 1 TO CLOSE THE LOOP
        
        // RENDER VENUS ORBIT
        glBindVertexArray(venusOrbitVAO);
        glDrawArrays(GL_LINE_LOOP, 0, 129); // 128 SEGMENTS + 1 TO CLOSE THE LOOP
        
        // RENDER EARTH ORBIT
        glBindVertexArray(earthOrbitVAO);
        glDrawArrays(GL_LINE_LOOP, 0, 129); // 128 SEGMENTS + 1 TO CLOSE THE LOOP
        
        // RENDER SUN
        glUseProgram(sunShaderProgram);
        
        // CREATE SUN TRANSFORMATION MATRIX WITH ROTATION
        Matrix4f sunModel = new Matrix4f();
        sunModel.rotateY(sun.getRotationAngle()); // ROTATE AROUND Y-AXIS
        
        Matrix4f sunMVP = new Matrix4f();
        mvpMatrix.mul(sunModel, sunMVP);
        
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        sunMVP.get(matrixBuffer);
        glUniformMatrix4fv(sunMvpLocation, false, matrixBuffer);
        glUniform3f(sunColorLocation, sun.getColor().x, sun.getColor().y, sun.getColor().z);
        
        glBindVertexArray(sun.getVAO());
        glDrawElements(GL_TRIANGLES, sun.getSphere().getIndices().length, GL_UNSIGNED_INT, 0);
        
        // RENDER MERCURY
        glUseProgram(sunShaderProgram); // SAME SHADER AS SUN FOR NOW
        
        // CREATE MERCURY TRANSFORMATION MATRIX
        Matrix4f projection = new Matrix4f();
        projection.perspective((float) Math.toRadians(55.0f), (float) width[0] / height[0], 1.0f, 100000.0f);
        
        // CALCULATE LOOK-AT TARGET BASED ON CAMERA MODE
        float lookX, lookY, lookZ;
        
        if (cameraTrackingEnabled && !"NONE".equals(trackedObject)) {
            // WHEN TRACKING, ALWAYS LOOK AT THE TRACKED OBJECT
            Vector3f targetPosition = new Vector3f();
            switch (trackedObject) {
                case "SUN":
                    targetPosition.set(0, 0, 0);
                    break;
                case "MERCURY":
                    targetPosition.set(mercury.getPosition());
                    break;
                case "VENUS":
                    targetPosition.set(venus.getPosition());
                    break;
                case "EARTH":
                    targetPosition.set(earth.getPosition());
                    break;
                default:
                    targetPosition.set(0, 0, 0);
            }
            lookX = targetPosition.x;
            lookY = targetPosition.y;
            lookZ = targetPosition.z;
        } else {
            // FREE CAMERA MODE - CALCULATE LOOK-AT TARGET BASED ON CAMERA ROTATION (FPS STYLE)
            float radPitch = (float) Math.toRadians(cameraPitch);
            float radYaw = (float) Math.toRadians(cameraYaw);
            
            lookX = cameraX + (float) (Math.cos(radPitch) * Math.sin(radYaw));
            lookY = cameraY - (float) Math.sin(radPitch);
            lookZ = cameraZ + (float) (Math.cos(radPitch) * Math.cos(radYaw));
        }
        
        Matrix4f view = new Matrix4f();
        view.lookAt(cameraX, cameraY, cameraZ, lookX, lookY, lookZ, 0, 1, 0);
        
        Matrix4f mercuryModel = new Matrix4f();
        mercuryModel.translate(mercury.getPosition()); // TRANSLATE TO MERCURY'S ORBIT POSITION
        mercuryModel.rotateY(mercury.getRotationAngle()); // ADD PLANET ROTATION
        
        Matrix4f mercuryMVP = new Matrix4f();
        projection.mul(view, mercuryMVP);
        mercuryMVP.mul(mercuryModel);
        
        FloatBuffer matrixBufferMercury = BufferUtils.createFloatBuffer(16);
        mercuryMVP.get(matrixBufferMercury);
        glUniformMatrix4fv(sunMvpLocation, false, matrixBufferMercury);
        glUniform3f(sunColorLocation, mercury.getColor().x, mercury.getColor().y, mercury.getColor().z);
        
        glBindVertexArray(mercury.getVAO());
        glDrawElements(GL_TRIANGLES, mercury.getSphere().getIndices().length, GL_UNSIGNED_INT, 0);
        
        // RENDER VENUS
        glUseProgram(sunShaderProgram); // SAME SHADER AS SUN FOR NOW
        
        // CREATE VENUS TRANSFORMATION MATRIX
        Matrix4f venusModel = new Matrix4f();
        venusModel.translate(venus.getPosition()); // TRANSLATE TO VENUS'S ORBIT POSITION
        venusModel.rotateY(venus.getRotationAngle()); // ADD PLANET ROTATION (RETROGRADE)
        
        Matrix4f venusMVP = new Matrix4f();
        projection.mul(view, venusMVP);
        venusMVP.mul(venusModel);
        
        FloatBuffer matrixBufferVenus = BufferUtils.createFloatBuffer(16);
        venusMVP.get(matrixBufferVenus);
        glUniformMatrix4fv(sunMvpLocation, false, matrixBufferVenus);
        glUniform3f(sunColorLocation, venus.getColor().x, venus.getColor().y, venus.getColor().z);
        
        glBindVertexArray(venus.getVAO());
        glDrawElements(GL_TRIANGLES, venus.getSphere().getIndices().length, GL_UNSIGNED_INT, 0);
        
        // RENDER EARTH
        glUseProgram(sunShaderProgram); // SAME SHADER AS SUN FOR NOW
        
        // CREATE EARTH TRANSFORMATION MATRIX
        Matrix4f earthModel = new Matrix4f();
        earthModel.translate(earth.getPosition()); // TRANSLATE TO EARTH'S ORBIT POSITION
        earthModel.rotateY(earth.getRotationAngle()); // ADD PLANET ROTATION (24 HOURS)
        
        Matrix4f earthMVP = new Matrix4f();
        projection.mul(view, earthMVP);
        earthMVP.mul(earthModel);
        
        FloatBuffer matrixBufferEarth = BufferUtils.createFloatBuffer(16);
        earthMVP.get(matrixBufferEarth);
        glUniformMatrix4fv(sunMvpLocation, false, matrixBufferEarth);
        glUniform3f(sunColorLocation, earth.getColor().x, earth.getColor().y, earth.getColor().z);
        
        glBindVertexArray(earth.getVAO());
        glDrawElements(GL_TRIANGLES, earth.getSphere().getIndices().length, GL_UNSIGNED_INT, 0);
        
        // RENDER UI (2D OVERLAY)
        renderUI(width[0], height[0]);
        
        glBindVertexArray(0);
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public void destroy() {
        glDeleteVertexArrays(gridVAO);
        glDeleteBuffers(gridVBO);
        glDeleteVertexArrays(orbitVAO);
        glDeleteBuffers(orbitVBO);
        glDeleteVertexArrays(sun.getVAO());
        glDeleteBuffers(sun.getVBO());
        glDeleteBuffers(sun.getEBO());
        glDeleteVertexArrays(mercury.getVAO());
        glDeleteBuffers(mercury.getVBO());
        glDeleteBuffers(mercury.getEBO());
        glDeleteVertexArrays(venus.getVAO());
        glDeleteBuffers(venus.getVBO());
        glDeleteBuffers(venus.getEBO());
        glDeleteVertexArrays(earth.getVAO());
        glDeleteBuffers(earth.getVBO());
        glDeleteBuffers(earth.getEBO());
        glDeleteVertexArrays(uiVAO);
        glDeleteBuffers(uiVBO);
        glDeleteVertexArrays(textVAO);
        glDeleteBuffers(textVBO);
        glDeleteProgram(gridShaderProgram);
        glDeleteProgram(sunShaderProgram);
        glDeleteProgram(uiShaderProgram);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }
    
    private boolean isPointInButton(double mouseX, double mouseY) {
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
    
    private boolean isPointInPauseButton(double mouseX, double mouseY) {
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
    
    private boolean isPointInTrackingButton(double mouseX, double mouseY, String buttonType) {
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, width, height);
        
        // CONVERT GLFW COORDINATES (TOP-LEFT ORIGIN) TO BUTTON COORDINATES (BOTTOM-LEFT ORIGIN)
        double adjustedY = height[0] - mouseY;
        
        // DETERMINE BUTTON X POSITION BASED ON TYPE
        float buttonX;
        switch (buttonType) {
            case "SUN":
                buttonX = SUN_BUTTON_X;
                break;
            case "MERCURY":
                buttonX = MERCURY_BUTTON_X;
                break;
            case "VENUS":
                buttonX = VENUS_BUTTON_X;
                break;
            case "EARTH":
                buttonX = EARTH_BUTTON_X;
                break;
            default:
                return false;
        }
        
        // CHECK IF POINT IS INSIDE TRACKING BUTTONS BOUNDS
        return mouseX >= buttonX && mouseX <= buttonX + TRACK_BUTTON_WIDTH &&
               adjustedY >= TRACK_BUTTON_Y && adjustedY <= TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT;
    }
    
    private void setTrackedObject(String objectName) {
        if (trackedObject.equals(objectName)) {
            // CLICKING THE SAME BUTTON TOGGLES TRACKING OFF
            trackedObject = "NONE";
            cameraTrackingEnabled = false;
            System.out.println("Camera tracking disabled");
        } else {
            // CLICKING A DIFFERENT BUTTON ENABLES TRACKING FOR THAT OBJECT
            trackedObject = objectName;
            cameraTrackingEnabled = true;
            trackingZoomDistance = 1.0f; // RESET ZOOM TO DEFAULT WHEN SWITCHING TARGETS
            System.out.println("Camera now tracking " + objectName);
        }
    }
    
    private void renderUI(int windowWidth, int windowHeight) {
        // DISABLE DEPTH TESTING FOR 2D UI
        glDisable(GL_DEPTH_TEST);
        
        // ENABLE BLENDING FOR TRANSPARENCY
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        // CREATE ORTHOGRAPHIC PROJECTION FOR 2D UI
        Matrix4f orthoMatrix = new Matrix4f();
        orthoMatrix.ortho(0, windowWidth, 0, windowHeight, -1, 1);
        
        // RENDER BUTTON
        glUseProgram(uiShaderProgram);
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        orthoMatrix.get(matrixBuffer);
        glUniformMatrix4fv(uiMvpLocation, false, matrixBuffer);
        
        // SET BUTTON COLOR (GREEN IF GRID ON, RED IF GRID OFF)
        if (gridVisible) {
            glUniform3f(uiColorLocation, 0.2f, 0.8f, 0.2f); // GREEN
        } else {
            glUniform3f(uiColorLocation, 0.8f, 0.2f, 0.2f); // RED
        }
        
        glBindVertexArray(uiVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4); // DRAW BUTTON AS QUAD
        
        // RENDER GRID ICON
        glUniform3f(uiColorLocation, 1.0f, 1.0f, 1.0f); // WHITE ICON
        glBindVertexArray(textVAO);
        glDrawArrays(GL_TRIANGLES, 0, 54); // 9 SQUARES * 6 VERTICES = 54 VERTICES
        
        // RENDER PAUSE BUTTON
        if (orbitalMotionPaused) {
            glUniform3f(uiColorLocation, 0.8f, 0.2f, 0.2f); // RED WHEN PAUSED
        } else {
            glUniform3f(uiColorLocation, 0.2f, 0.8f, 0.2f); // GREEN WHEN PLAYING
        }
        
        glBindVertexArray(pauseButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4); // DRAW PAUSE BUTTON AS QUAD
        
        // RENDER PAUSE ICON
        glUniform3f(uiColorLocation, 1.0f, 1.0f, 1.0f); // WHITE ICON
        glBindVertexArray(pauseIconVAO);
        glDrawArrays(GL_TRIANGLES, 0, 12); // 2 BARS * 6 VERTICES = 12 VERTICES
        
        // RENDER TRACKING BUTTONS
        renderTrackingButtons();
        
        // RE-ENABLE DEPTH TESTING
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }
    
    private void updateOrbitalMotion() {
        // SKIP UPDATES IF PAUSED
        if (orbitalMotionPaused) {
            return;
        }
        
        // CALCULATE DELTA TIME
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastTime) / 1_000_000_000.0f; // CONVERT TO SECONDS
        lastTime = currentTime;
        
        // APPLY TIME ACCELERATION
        deltaTime *= TIME_ACCELERATION;
        
        // UPDATE PLANETARY POSITIONS AND ROTATIONS
        sun.updateRotation(deltaTime);
        mercury.updateOrbitalPosition(deltaTime);
        venus.updateOrbitalPosition(deltaTime);
        earth.updateOrbitalPosition(deltaTime);
    }
    
    private void createTrackingButtons() {
        // SUN BUTTON
        float[] sunButtonVertices = {
            SUN_BUTTON_X, TRACK_BUTTON_Y,                           // BOTTOM LEFT
            SUN_BUTTON_X + TRACK_BUTTON_WIDTH, TRACK_BUTTON_Y,      // BOTTOM RIGHT
            SUN_BUTTON_X + TRACK_BUTTON_WIDTH, TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT,  // TOP RIGHT
            SUN_BUTTON_X, TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT      // TOP LEFT
        };
        
        sunButtonVAO = glGenVertexArrays();
        sunButtonVBO = glGenBuffers();
        
        glBindVertexArray(sunButtonVAO);
        glBindBuffer(GL_ARRAY_BUFFER, sunButtonVBO);
        
        FloatBuffer sunBuffer = BufferUtils.createFloatBuffer(sunButtonVertices.length);
        sunBuffer.put(sunButtonVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, sunBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        
        // MERCURY BUTTON
        float[] mercuryButtonVertices = {
            MERCURY_BUTTON_X, TRACK_BUTTON_Y,                           // BOTTOM LEFT
            MERCURY_BUTTON_X + TRACK_BUTTON_WIDTH, TRACK_BUTTON_Y,      // BOTTOM RIGHT
            MERCURY_BUTTON_X + TRACK_BUTTON_WIDTH, TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT,  // TOP RIGHT
            MERCURY_BUTTON_X, TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT      // TOP LEFT
        };
        
        mercuryButtonVAO = glGenVertexArrays();
        mercuryButtonVBO = glGenBuffers();
        
        glBindVertexArray(mercuryButtonVAO);
        glBindBuffer(GL_ARRAY_BUFFER, mercuryButtonVBO);
        
        FloatBuffer mercuryBuffer = BufferUtils.createFloatBuffer(mercuryButtonVertices.length);
        mercuryBuffer.put(mercuryButtonVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, mercuryBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        
        // VENUS BUTTON
        float[] venusButtonVertices = {
            VENUS_BUTTON_X, TRACK_BUTTON_Y,                           // BOTTOM LEFT
            VENUS_BUTTON_X + TRACK_BUTTON_WIDTH, TRACK_BUTTON_Y,      // BOTTOM RIGHT
            VENUS_BUTTON_X + TRACK_BUTTON_WIDTH, TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT,  // TOP RIGHT
            VENUS_BUTTON_X, TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT      // TOP LEFT
        };
        
        venusButtonVAO = glGenVertexArrays();
        venusButtonVBO = glGenBuffers();
        
        glBindVertexArray(venusButtonVAO);
        glBindBuffer(GL_ARRAY_BUFFER, venusButtonVBO);
        
        FloatBuffer venusBuffer = BufferUtils.createFloatBuffer(venusButtonVertices.length);
        venusBuffer.put(venusButtonVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, venusBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        
        // EARTH BUTTON
        float[] earthButtonVertices = {
            EARTH_BUTTON_X, TRACK_BUTTON_Y,                           // BOTTOM LEFT
            EARTH_BUTTON_X + TRACK_BUTTON_WIDTH, TRACK_BUTTON_Y,      // BOTTOM RIGHT
            EARTH_BUTTON_X + TRACK_BUTTON_WIDTH, TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT,  // TOP RIGHT
            EARTH_BUTTON_X, TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT      // TOP LEFT
        };
        
        earthButtonVAO = glGenVertexArrays();
        earthButtonVBO = glGenBuffers();
        
        glBindVertexArray(earthButtonVAO);
        glBindBuffer(GL_ARRAY_BUFFER, earthButtonVBO);
        
        FloatBuffer earthBuffer = BufferUtils.createFloatBuffer(earthButtonVertices.length);
        earthBuffer.put(earthButtonVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, earthBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        
        // CREATE BUTTON LABELS (PLACEHOLDER FOR FUTURE TEXT RENDERING)
        createTrackingButtonLabels();
    }
    
    private void createTrackingButtonLabels() {
        // CREATE SIMPLE TEXT LABELS FOR EACH TRACKING BUTTON
        // SUN LABEL - "S"
        float sunLabelX = SUN_BUTTON_X + TRACK_BUTTON_WIDTH / 2 - 3;
        float sunLabelY = TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT / 2 - 4;
        
        // MERCURY LABEL - "M"  
        float mercuryLabelX = MERCURY_BUTTON_X + TRACK_BUTTON_WIDTH / 2 - 3;
        float mercuryLabelY = TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT / 2 - 4;
        
        // VENUS LABEL - "V"
        float venusLabelX = VENUS_BUTTON_X + TRACK_BUTTON_WIDTH / 2 - 3;
        float venusLabelY = TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT / 2 - 4;
        
        // EARTH LABEL - "E"
        float earthLabelX = EARTH_BUTTON_X + TRACK_BUTTON_WIDTH / 2 - 3;
        float earthLabelY = TRACK_BUTTON_Y + TRACK_BUTTON_HEIGHT / 2 - 4;
        
        // FOR SIMPLICITY, WE'LL CREATE LETTER SHAPES USING SMALL RECTANGLES
        // THESE WILL BE RENDERED AS WHITE TEXT OVER THE BUTTONS
        System.out.println("Tracking buttons created: Sun (S), Mercury (M), Venus (V), Earth (E)");
    }
    
    private void updateCameraTracking() {
        if (!cameraTrackingEnabled || "NONE".equals(trackedObject)) {
            return;
        }
        
        Vector3f targetPosition = new Vector3f();
        float viewingDistance = 2000.0f; // DEFAULT VIEWING DISTANCE
        
        // GET TARGET POSITION AND APPROPRIATE VIEWING DISTANCE
        switch (trackedObject) {
            case "SUN":
                targetPosition.set(0, 0, 0); // SUN IS AT ORIGIN
                viewingDistance = 400.0f; // CLOSER BASE DISTANCE FOR SUN
                break;
            case "MERCURY":
                targetPosition.set(mercury.getPosition());
                viewingDistance = 150.0f; // CLOSER FOR SMALL PLANET
                break;
            case "VENUS":
                targetPosition.set(venus.getPosition());
                viewingDistance = 200.0f; // CLOSER FOR MEDIUM PLANET
                break;
            case "EARTH":
                targetPosition.set(earth.getPosition());
                viewingDistance = 800.0f; // LARGER DISTANCE FOR EARTH SINCE IT'S A BIGGER PLANET
                break;
            default:
                return;
        }
        
        // APPLY ZOOM MULTIPLIER
        viewingDistance *= trackingZoomDistance;
        
        // CALCULATE CAMERA POSITION USING SPHERICAL COORDINATES AROUND TARGET
        // USE CURRENT YAW AND PITCH TO MAINTAIN ORBITAL CAMERA POSITION
        float x = targetPosition.x + viewingDistance * (float) (Math.cos(cameraPitch) * Math.sin(cameraYaw));
        float y = targetPosition.y + viewingDistance * (float) Math.sin(cameraPitch);
        float z = targetPosition.z + viewingDistance * (float) (Math.cos(cameraPitch) * Math.cos(cameraYaw));
        
        cameraX = x;
        cameraY = y;
        cameraZ = z;
        
        // CAMERA ALWAYS LOOKS AT THE TARGET (NO NEED TO RECALCULATE YAW/PITCH)
        // THE YAW/PITCH VALUES ARE USED FOR POSITIONING, NOT LOOKING DIRECTION
    }
    
    private void renderTrackingButtons() {
        // SUN BUTTON
        if ("SUN".equals(trackedObject)) {
            glUniform3f(uiColorLocation, 1.0f, 0.8f, 0.2f); // GOLD WHEN TRACKING SUN
        } else {
            glUniform3f(uiColorLocation, 0.3f, 0.3f, 0.3f); // GRAY WHEN NOT TRACKING
        }
        glBindVertexArray(sunButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        
        // MERCURY BUTTON
        if ("MERCURY".equals(trackedObject)) {
            glUniform3f(uiColorLocation, 0.7f, 0.7f, 0.7f); // LIGHT GRAY WHEN TRACKING MERCURY
        } else {
            glUniform3f(uiColorLocation, 0.3f, 0.3f, 0.3f); // GRAY WHEN NOT TRACKING
        }
        glBindVertexArray(mercuryButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        
        // VENUS BUTTON
        if ("VENUS".equals(trackedObject)) {
            glUniform3f(uiColorLocation, 1.0f, 0.6f, 0.0f); // ORANGE WHEN TRACKING VENUS
        } else {
            glUniform3f(uiColorLocation, 0.3f, 0.3f, 0.3f); // GRAY WHEN NOT TRACKING
        }
        glBindVertexArray(venusButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        
        // EARTH BUTTON
        if ("EARTH".equals(trackedObject)) {
            glUniform3f(uiColorLocation, 0.2f, 0.6f, 1.0f); // BLUE WHEN TRACKING EARTH
        } else {
            glUniform3f(uiColorLocation, 0.3f, 0.3f, 0.3f); // GRAY WHEN NOT TRACKING
        }
        glBindVertexArray(earthButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }
}