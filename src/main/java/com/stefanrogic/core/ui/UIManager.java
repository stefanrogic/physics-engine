package com.stefanrogic.core.ui;

import com.stefanrogic.core.rendering.ShaderManager;
import com.stefanrogic.core.input.Camera;
import com.stefanrogic.core.scene.SceneManager;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;

/**
 * Manages UI creation, rendering and layout constants
 */
public class UIManager {
    
    private ShaderManager.ShaderPrograms shaders;
    private Camera camera;
    private SceneManager sceneManager;
    
    // UI ELEMENTS
    private int uiVAO, uiVBO;
    private int pauseButtonVAO, pauseButtonVBO;
    private int textVAO, textVBO;
    private int pauseIconVAO, pauseIconVBO;
    private int playIconVAO, playIconVBO;
    
    // TRACKING BUTTON VAOs
    private int sunButtonVAO, sunButtonVBO;
    private int mercuryButtonVAO, mercuryButtonVBO;
    private int venusButtonVAO, venusButtonVBO;
    private int earthButtonVAO, earthButtonVBO;
    private int marsButtonVAO, marsButtonVBO;
    
    // UI BUTTON COORDINATES - GRID BUTTON
    public static final float BUTTON_X = 20.0f;
    public static final float BUTTON_Y = 20.0f;
    public static final float BUTTON_WIDTH = 40.0f;
    public static final float BUTTON_HEIGHT = 40.0f;
    
    // UI BUTTON COORDINATES - PAUSE BUTTON
    public static final float PAUSE_BUTTON_X = 70.0f; // NEXT TO GRID BUTTON
    public static final float PAUSE_BUTTON_Y = 20.0f;
    public static final float PAUSE_BUTTON_WIDTH = 40.0f;
    public static final float PAUSE_BUTTON_HEIGHT = 40.0f;
    
    // UI BUTTON COORDINATES - TRACKING BUTTONS (ROW BELOW)
    public static final float TRACK_BUTTON_WIDTH = 30.0f;
    public static final float TRACK_BUTTON_HEIGHT = 25.0f;
    public static final float TRACK_BUTTON_Y = 70.0f; // BELOW MAIN BUTTONS
    public static final float SUN_BUTTON_X = 20.0f;
    public static final float MERCURY_BUTTON_X = 55.0f;
    public static final float VENUS_BUTTON_X = 90.0f;
    public static final float EARTH_BUTTON_X = 125.0f;
    // MARS SYSTEM BUTTONS (SECOND ROW) - REMOVED INDIVIDUAL MOON BUTTONS TO REDUCE CLUTTER
    public static final float TRACK_BUTTON_Y2 = 100.0f; // SECOND ROW
    public static final float MARS_BUTTON_X = 20.0f;
    
    // UI state
    private boolean gridVisible = false;
    
    public UIManager(ShaderManager.ShaderPrograms shaders, Camera camera, SceneManager sceneManager) {
        this.shaders = shaders;
        this.camera = camera;
        this.sceneManager = sceneManager;
    }
    
    /**
     * Initialize all UI elements
     */
    public void createUI() {
        createMainButtons();
        createTextGeometry();
        createPauseIcon();
        createPlayIcon();
        createTrackingButtons();
        createTrackingButtonLabels();
    }
    
    private void createMainButtons() {
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
    
    private void createPauseIcon() {
        float pauseIconX = PAUSE_BUTTON_X + 13.0f; // PROPERLY CENTERED FOR PAUSE BARS (40-14)/2 = 13
        float pauseIconY = PAUSE_BUTTON_Y + 10.0f; // PROPERLY CENTERED FOR PAUSE BARS (40-20)/2 = 10
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
    
    private void createPlayIcon() {
        float playIconX = PAUSE_BUTTON_X + 12.0f; // PROPERLY CENTERED FOR PLAY TRIANGLE (40-16)/2 = 12
        float playIconY = PAUSE_BUTTON_Y + 12.0f; // PROPERLY CENTERED FOR PLAY TRIANGLE (40-16)/2 = 12
        float triangleSize = 16.0f;
        
        java.util.List<Float> playIconVertices = new java.util.ArrayList<>();
        
        // PLAY TRIANGLE (POINTING RIGHT)
        float leftX = playIconX;
        float rightX = playIconX + triangleSize;
        float topY = playIconY + triangleSize;
        float bottomY = playIconY;
        float middleY = playIconY + triangleSize / 2.0f;
        
        // TRIANGLE AS THREE VERTICES
        playIconVertices.addAll(java.util.Arrays.asList(
            leftX, topY,           // TOP LEFT
            leftX, bottomY,        // BOTTOM LEFT  
            rightX, middleY        // RIGHT POINT
        ));
        
        // CONVERT TO ARRAY
        float[] playIconArray = new float[playIconVertices.size()];
        for (int i = 0; i < playIconVertices.size(); i++) {
            playIconArray[i] = playIconVertices.get(i);
        }
        
        playIconVAO = glGenVertexArrays();
        playIconVBO = glGenBuffers();
        
        glBindVertexArray(playIconVAO);
        glBindBuffer(GL_ARRAY_BUFFER, playIconVBO);
        
        FloatBuffer playIconBuffer = BufferUtils.createFloatBuffer(playIconArray.length);
        playIconBuffer.put(playIconArray).flip();
        glBufferData(GL_ARRAY_BUFFER, playIconBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }

    private void createTrackingButtons() {
        // SUN BUTTON
        createTrackingButton(SUN_BUTTON_X, TRACK_BUTTON_Y, 
                           sunButtonVAO = glGenVertexArrays(), 
                           sunButtonVBO = glGenBuffers());
        
        // MERCURY BUTTON  
        createTrackingButton(MERCURY_BUTTON_X, TRACK_BUTTON_Y,
                           mercuryButtonVAO = glGenVertexArrays(),
                           mercuryButtonVBO = glGenBuffers());
        
        // VENUS BUTTON
        createTrackingButton(VENUS_BUTTON_X, TRACK_BUTTON_Y,
                           venusButtonVAO = glGenVertexArrays(),
                           venusButtonVBO = glGenBuffers());
        
        // EARTH BUTTON
        createTrackingButton(EARTH_BUTTON_X, TRACK_BUTTON_Y,
                           earthButtonVAO = glGenVertexArrays(),
                           earthButtonVBO = glGenBuffers());
        
        // MARS BUTTON (SECOND ROW)
        createTrackingButton(MARS_BUTTON_X, TRACK_BUTTON_Y2,
                           marsButtonVAO = glGenVertexArrays(),
                           marsButtonVBO = glGenBuffers());
    }
    
    private void createTrackingButton(float x, float y, int VAO, int VBO) {
        float[] buttonVertices = {
            x, y,                                               // BOTTOM LEFT
            x + TRACK_BUTTON_WIDTH, y,                          // BOTTOM RIGHT
            x + TRACK_BUTTON_WIDTH, y + TRACK_BUTTON_HEIGHT,    // TOP RIGHT
            x, y + TRACK_BUTTON_HEIGHT                          // TOP LEFT
        };
        
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        
        FloatBuffer buffer = BufferUtils.createFloatBuffer(buttonVertices.length);
        buffer.put(buttonVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    private void createTrackingButtonLabels() {
        // CREATE SIMPLE TEXT LABELS FOR EACH TRACKING BUTTON
        // FOR SIMPLICITY, WE'LL CREATE LETTER SHAPES USING SMALL RECTANGLES
        // THESE WILL BE RENDERED AS WHITE TEXT OVER THE BUTTONS
        System.out.println("Tracking buttons created: Sun (S), Mercury (M), Venus (V), Earth (E)");
    }
    
    /**
     * Render all UI elements
     */
    public void renderUI(int windowWidth, int windowHeight) {
        // DISABLE DEPTH TESTING FOR 2D UI
        glDisable(GL_DEPTH_TEST);
        
        // ENABLE BLENDING FOR TRANSPARENCY
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        // CREATE ORTHOGRAPHIC PROJECTION FOR 2D UI
        Matrix4f orthoMatrix = new Matrix4f();
        orthoMatrix.ortho(0, windowWidth, 0, windowHeight, -1, 1);
        
        // RENDER BUTTON
        glUseProgram(shaders.uiShaderProgram);
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        orthoMatrix.get(matrixBuffer);
        glUniformMatrix4fv(shaders.uiMvpLocation, false, matrixBuffer);
        
        // SET BUTTON COLOR (GREEN IF GRID ON, RED IF GRID OFF)
        if (gridVisible) {
            glUniform3f(shaders.uiColorLocation, 0.2f, 0.8f, 0.2f); // GREEN
        } else {
            glUniform3f(shaders.uiColorLocation, 0.8f, 0.2f, 0.2f); // RED
        }
        
        glBindVertexArray(uiVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4); // DRAW BUTTON AS QUAD
        
        // RENDER GRID ICON
        glUniform3f(shaders.uiColorLocation, 1.0f, 1.0f, 1.0f); // WHITE ICON
        glBindVertexArray(textVAO);
        glDrawArrays(GL_TRIANGLES, 0, 54); // 9 SQUARES * 6 VERTICES = 54 VERTICES
        
        // RENDER PAUSE BUTTON
        if (sceneManager.isOrbitalMotionPaused()) {
            glUniform3f(shaders.uiColorLocation, 0.8f, 0.2f, 0.2f); // RED WHEN PAUSED
        } else {
            glUniform3f(shaders.uiColorLocation, 0.2f, 0.8f, 0.2f); // GREEN WHEN PLAYING
        }
        
        glBindVertexArray(pauseButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4); // DRAW PAUSE BUTTON AS QUAD
        
        // RENDER PAUSE/PLAY ICON
        glUniform3f(shaders.uiColorLocation, 1.0f, 1.0f, 1.0f); // WHITE ICON
        if (sceneManager.isOrbitalMotionPaused()) {
            // SHOW PLAY ICON WHEN PAUSED
            glBindVertexArray(playIconVAO);
            glDrawArrays(GL_TRIANGLES, 0, 3); // TRIANGLE WITH 3 VERTICES
        } else {
            // SHOW PAUSE ICON WHEN PLAYING
            glBindVertexArray(pauseIconVAO);
            glDrawArrays(GL_TRIANGLES, 0, 12); // 2 BARS * 6 VERTICES = 12 VERTICES
        }
        
        // RENDER TRACKING BUTTONS
        renderTrackingButtons();
        
        // RE-ENABLE DEPTH TESTING
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }
    
    private void renderTrackingButtons() {
        // SUN BUTTON
        if ("SUN".equals(camera.getTrackedObject())) {
            glUniform3f(shaders.uiColorLocation, 1.0f, 0.8f, 0.2f); // GOLD WHEN TRACKING SUN
        } else {
            glUniform3f(shaders.uiColorLocation, 0.3f, 0.3f, 0.3f); // GRAY WHEN NOT TRACKING
        }
        glBindVertexArray(sunButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        
        // MERCURY BUTTON
        if ("MERCURY".equals(camera.getTrackedObject())) {
            glUniform3f(shaders.uiColorLocation, 0.7f, 0.7f, 0.7f); // LIGHT GRAY WHEN TRACKING MERCURY
        } else {
            glUniform3f(shaders.uiColorLocation, 0.3f, 0.3f, 0.3f); // GRAY WHEN NOT TRACKING
        }
        glBindVertexArray(mercuryButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        
        // VENUS BUTTON
        if ("VENUS".equals(camera.getTrackedObject())) {
            glUniform3f(shaders.uiColorLocation, 1.0f, 0.6f, 0.0f); // ORANGE WHEN TRACKING VENUS
        } else {
            glUniform3f(shaders.uiColorLocation, 0.3f, 0.3f, 0.3f); // GRAY WHEN NOT TRACKING
        }
        glBindVertexArray(venusButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        
        // EARTH BUTTON
        if ("EARTH".equals(camera.getTrackedObject())) {
            glUniform3f(shaders.uiColorLocation, 0.2f, 0.6f, 1.0f); // BLUE WHEN TRACKING EARTH
        } else {
            glUniform3f(shaders.uiColorLocation, 0.3f, 0.3f, 0.3f); // GRAY WHEN NOT TRACKING
        }
        glBindVertexArray(earthButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        
        // MARS BUTTON
        if ("MARS".equals(camera.getTrackedObject())) {
            glUniform3f(shaders.uiColorLocation, 1.0f, 0.5f, 0.0f); // ORANGE WHEN TRACKING MARS
        } else {
            glUniform3f(shaders.uiColorLocation, 0.3f, 0.3f, 0.3f); // GRAY WHEN NOT TRACKING
        }
        glBindVertexArray(marsButtonVAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }
    
    // Getters for state
    public boolean isGridVisible() {
        return gridVisible;
    }
    
    public void setGridVisible(boolean visible) {
        this.gridVisible = visible;
    }
    
    /**
     * Clean up all UI resources
     */
    public void cleanup() {
        glDeleteVertexArrays(uiVAO);
        glDeleteBuffers(uiVBO);
        glDeleteVertexArrays(pauseButtonVAO);
        glDeleteBuffers(pauseButtonVBO);
        glDeleteVertexArrays(textVAO);
        glDeleteBuffers(textVBO);
        glDeleteVertexArrays(pauseIconVAO);
        glDeleteBuffers(pauseIconVBO);
        glDeleteVertexArrays(playIconVAO);
        glDeleteBuffers(playIconVBO);
        
        // Clean up tracking buttons
        glDeleteVertexArrays(sunButtonVAO);
        glDeleteBuffers(sunButtonVBO);
        glDeleteVertexArrays(mercuryButtonVAO);
        glDeleteBuffers(mercuryButtonVBO);
        glDeleteVertexArrays(venusButtonVAO);
        glDeleteBuffers(venusButtonVBO);
        glDeleteVertexArrays(earthButtonVAO);
        glDeleteBuffers(earthButtonVBO);
        glDeleteVertexArrays(marsButtonVAO);
        glDeleteBuffers(marsButtonVBO);
    }
}
