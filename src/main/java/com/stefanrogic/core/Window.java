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

import com.stefanrogic.objects.Sun;
import com.stefanrogic.objects.Mercury;

public class Window {
    private long windowHandle;
    private int gridShaderProgram, sunShaderProgram;
    private int gridVAO, gridVBO;
    private int orbitVAO, orbitVBO; // FOR ORBITAL PATHS
    private int gridMvpLocation, sunMvpLocation, sunColorLocation;
    
    // CAMERA SETTINGS - FPS STYLE
    private float cameraX = 0.0f;
    private float cameraY = 200.0f; // START ABOVE THE GRID
    private float cameraZ = 500.0f;
    private float cameraPitch = 0.0f; // UP/DOWN ROTATION
    private float cameraYaw = 0.0f;   // LEFT/RIGHT ROTATION
    
    // MOUSE CONTROLS
    private boolean mousePressed = false;
    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    private final float MOUSE_SENSITIVITY = 0.3f;
    
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
    
    // UI ELEMENTS
    private int uiShaderProgram;
    private int uiVAO, uiVBO;
    private int textVAO, textVBO; // FOR TEXT RENDERING
    private int uiMvpLocation, uiColorLocation;
    private final float BUTTON_X = 20.0f;
    private final float BUTTON_Y = 20.0f;
    private final float BUTTON_WIDTH = 40.0f;
    private final float BUTTON_HEIGHT = 40.0f;
    
    // SUN AND PLANETS
    private Sun sun;
    private Mercury mercury;
    
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
                    
                    // CHECK IF CLICK IS ON BUTTON
                    if (isPointInButton(xpos[0], ypos[0])) {
                        gridVisible = !gridVisible; // TOGGLE GRID
                        System.out.println("Grid " + (gridVisible ? "ON" : "OFF"));
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
                
                cameraYaw -= (float) deltaX * MOUSE_SENSITIVITY; // INVERTED X FOR NATURAL ROTATION
                cameraPitch += (float) deltaY * MOUSE_SENSITIVITY; // NORMAL Y FOR NATURAL UP/DOWN
                
                // CLAMP PITCH TO PREVENT FLIPPING
                cameraPitch = Math.max(-89.0f, Math.min(89.0f, cameraPitch));
                
                lastMouseX = xpos;
                lastMouseY = ypos;
            }
        });
        
        // SCROLL WHEEL - REMOVE OR REPURPOSE (OPTIONAL: COULD ADJUST MOVEMENT SPEED)
        glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) -> {
            // OPTIONAL: ADJUST MOVEMENT SPEED WITH SCROLL WHEEL
            // movementSpeed += (float) yoffset * 2.0f;
            // movementSpeed = Math.max(1.0f, Math.min(50.0f, movementSpeed));
        });
    }
    
    private void updateCameraMovement() {
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
        // EXPANDED GRID FOR NEW SCALE (1 UNIT = 10,000 KM)
        int gridSize = 100; // MUCH LARGER GRID
        float spacing = 200.0f; // LARGER SPACING (2 MILLION KM PER GRID SQUARE)
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
        
        // CREATE TEXT GEOMETRY FOR "GRID"
        createTextGeometry();
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
    }
    
    private void createOrbits() {
        // CREATE MERCURY ORBIT - CIRCULAR PATH AT MERCURY'S DISTANCE
        int orbitSegments = 128; // HIGH DETAIL FOR SMOOTH CIRCLE
        float mercuryDistance = mercury.getDistanceFromSun();
        
        // CREATE CIRCLE VERTICES
        java.util.List<Float> orbitVertices = new java.util.ArrayList<>();
        
        for (int i = 0; i <= orbitSegments; i++) {
            float angle = (float) (2.0 * Math.PI * i / orbitSegments);
            float x = (float) (mercuryDistance * Math.cos(angle));
            float z = (float) (mercuryDistance * Math.sin(angle));
            float y = 0.0f; // ORBIT ON XZ PLANE
            
            orbitVertices.add(x);
            orbitVertices.add(y);
            orbitVertices.add(z);
        }
        
        // CONVERT TO ARRAY
        float[] orbitArray = new float[orbitVertices.size()];
        for (int i = 0; i < orbitVertices.size(); i++) {
            orbitArray[i] = orbitVertices.get(i);
        }
        
        orbitVAO = glGenVertexArrays();
        orbitVBO = glGenBuffers();
        
        glBindVertexArray(orbitVAO);
        glBindBuffer(GL_ARRAY_BUFFER, orbitVBO);
        
        FloatBuffer orbitBuffer = BufferUtils.createFloatBuffer(orbitArray.length);
        orbitBuffer.put(orbitArray).flip();
        glBufferData(GL_ARRAY_BUFFER, orbitBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private Matrix4f createMVPMatrix(int width, int height) {
        Matrix4f projection = new Matrix4f();
        projection.perspective((float) Math.toRadians(45.0f), (float) width / height, 1.0f, 100000.0f); // INCREASED FAR PLANE FOR SOLAR SYSTEM SCALE
        
        // CALCULATE LOOK-AT TARGET BASED ON CAMERA ROTATION (FPS STYLE)
        float radPitch = (float) Math.toRadians(cameraPitch);
        float radYaw = (float) Math.toRadians(cameraYaw);
        
        // CALCULATE WHERE THE CAMERA IS LOOKING
        float lookX = cameraX + (float) (Math.cos(radPitch) * Math.sin(radYaw));
        float lookY = cameraY - (float) Math.sin(radPitch);
        float lookZ = cameraZ + (float) (Math.cos(radPitch) * Math.cos(radYaw));
        
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
            glDrawArrays(GL_LINES, 0, 404); // 101 * 4 VERTICES (UPDATED FOR NEW GRID SIZE)
        }
        
        // RENDER ORBITAL PATHS
        glUseProgram(gridShaderProgram); // USE SAME SHADER AS GRID FOR THIN LINES
        FloatBuffer orbitMatrixBuffer = BufferUtils.createFloatBuffer(16);
        mvpMatrix.get(orbitMatrixBuffer);
        glUniformMatrix4fv(gridMvpLocation, false, orbitMatrixBuffer);
        glBindVertexArray(orbitVAO);
        glDrawArrays(GL_LINE_LOOP, 0, 129); // 128 SEGMENTS + 1 TO CLOSE THE LOOP
        
        // RENDER SUN
        glUseProgram(sunShaderProgram);
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        mvpMatrix.get(matrixBuffer);
        glUniformMatrix4fv(sunMvpLocation, false, matrixBuffer);
        glUniform3f(sunColorLocation, sun.getColor().x, sun.getColor().y, sun.getColor().z);
        
        glBindVertexArray(sun.getVAO());
        glDrawElements(GL_TRIANGLES, sun.getSphere().getIndices().length, GL_UNSIGNED_INT, 0);
        
        // RENDER MERCURY
        glUseProgram(sunShaderProgram); // SAME SHADER AS SUN FOR NOW
        
        // CREATE MERCURY TRANSFORMATION MATRIX
        Matrix4f projection = new Matrix4f();
        projection.perspective((float) Math.toRadians(45.0f), (float) width[0] / height[0], 1.0f, 100000.0f);
        
        // CALCULATE LOOK-AT TARGET BASED ON CAMERA ROTATION (FPS STYLE)
        float radPitch = (float) Math.toRadians(cameraPitch);
        float radYaw = (float) Math.toRadians(cameraYaw);
        
        float lookX = cameraX + (float) (Math.cos(radPitch) * Math.sin(radYaw));
        float lookY = cameraY - (float) Math.sin(radPitch);
        float lookZ = cameraZ + (float) (Math.cos(radPitch) * Math.cos(radYaw));
        
        Matrix4f view = new Matrix4f();
        view.lookAt(cameraX, cameraY, cameraZ, lookX, lookY, lookZ, 0, 1, 0);
        
        Matrix4f mercuryModel = new Matrix4f();
        mercuryModel.translate(mercury.getPosition()); // TRANSLATE TO MERCURY'S ORBIT POSITION
        
        Matrix4f mercuryMVP = new Matrix4f();
        projection.mul(view, mercuryMVP);
        mercuryMVP.mul(mercuryModel);
        
        FloatBuffer matrixBufferMercury = BufferUtils.createFloatBuffer(16);
        mercuryMVP.get(matrixBufferMercury);
        glUniformMatrix4fv(sunMvpLocation, false, matrixBufferMercury);
        glUniform3f(sunColorLocation, mercury.getColor().x, mercury.getColor().y, mercury.getColor().z);
        
        glBindVertexArray(mercury.getVAO());
        glDrawElements(GL_TRIANGLES, mercury.getSphere().getIndices().length, GL_UNSIGNED_INT, 0);
        
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
        
        // RE-ENABLE DEPTH TESTING
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }
}