package com.stefanrogic.core.rendering;

import com.stefanrogic.core.scene.SceneManager;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;

/**
 * Manages orbital path creation and rendering
 */
public class OrbitRenderer {
    
    private SceneManager sceneManager;
    private ShaderManager.ShaderPrograms shaders;
    
    // ORBIT VAOs AND VBOs
    private int mercuryOrbitVAO, mercuryOrbitVBO;
    private int venusOrbitVAO, venusOrbitVBO;
    private int earthOrbitVAO, earthOrbitVBO;
    private int moonOrbitVAO, moonOrbitVBO;
    private int marsOrbitVAO, marsOrbitVBO;
    private int phobosOrbitVAO, phobosOrbitVBO;
    private int deimosOrbitVAO, deimosOrbitVBO;
    private int jupiterOrbitVAO, jupiterOrbitVBO;
    
    // JUPITER MOON ORBITS
    private int ioOrbitVAO, ioOrbitVBO;
    private int europaOrbitVAO, europaOrbitVBO;
    private int ganymedeOrbitVAO, ganymedeOrbitVBO;
    private int callistoOrbitVAO, callistoOrbitVBO;
    
    private static final int ORBIT_SEGMENTS = 128;
    
    public OrbitRenderer(SceneManager sceneManager, ShaderManager.ShaderPrograms shaders) {
        this.sceneManager = sceneManager;
        this.shaders = shaders;
    }
    
    /**
     * Create all orbital paths
     */
    public void createOrbits() {
        createMercuryOrbit();
        createVenusOrbit();
        createEarthOrbit();
        createMoonOrbit();
        createMarsOrbit();
        createPhobosOrbit();
        createDeimosOrbit();
        createJupiterOrbit();
        
        // CREATE JUPITER MOON ORBITS
        createIoOrbit();
        createEuropaOrbit();
        createGanymedeOrbit();
        createCallistoOrbit();
    }
    
    private void createMercuryOrbit() {
        float mercuryDistance = sceneManager.getMercury().getDistanceFromSun();
        mercuryOrbitVAO = createCircularOrbit(mercuryDistance);
        mercuryOrbitVBO = getCurrentVBO(); // Get the VBO that was just created
    }
    
    private void createVenusOrbit() {
        float venusDistance = sceneManager.getVenus().getDistanceFromSun();
        venusOrbitVAO = createCircularOrbit(venusDistance);
        venusOrbitVBO = getCurrentVBO();
    }
    
    private void createEarthOrbit() {
        float earthDistance = sceneManager.getEarth().getDistanceFromSun();
        earthOrbitVAO = createCircularOrbit(earthDistance);
        earthOrbitVBO = getCurrentVBO();
    }
    
    private void createMoonOrbit() {
        float moonDistance = sceneManager.getMoon().getDistanceFromEarth();
        moonOrbitVAO = createCircularOrbit(moonDistance);
        moonOrbitVBO = getCurrentVBO();
    }
    
    private void createMarsOrbit() {
        float marsDistance = sceneManager.getMars().getDistanceFromSun();
        marsOrbitVAO = createCircularOrbit(marsDistance);
        marsOrbitVBO = getCurrentVBO();
    }
    
    private void createPhobosOrbit() {
        float phobosDistance = sceneManager.getPhobos().getDistanceFromMars();
        phobosOrbitVAO = createCircularOrbit(phobosDistance);
        phobosOrbitVBO = getCurrentVBO();
    }
    
    private void createDeimosOrbit() {
        float deimosDistance = sceneManager.getDeimos().getDistanceFromMars();
        deimosOrbitVAO = createCircularOrbit(deimosDistance);
        deimosOrbitVBO = getCurrentVBO();
    }
    
    /**
     * Create a circular orbit with given radius
     */
    private int createCircularOrbit(float radius) {
        java.util.List<Float> orbitVertices = new java.util.ArrayList<>();
        
        for (int i = 0; i <= ORBIT_SEGMENTS; i++) {
            float angle = (float) (2.0 * Math.PI * i / ORBIT_SEGMENTS);
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
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
        
        int VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        
        FloatBuffer orbitBuffer = BufferUtils.createFloatBuffer(orbitArray.length);
        orbitBuffer.put(orbitArray).flip();
        glBufferData(GL_ARRAY_BUFFER, orbitBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        
        // Store the VBO for cleanup later
        currentVBO = VBO;
        
        return VAO;
    }
    
    private int currentVBO; // Helper to track the last created VBO
    private int getCurrentVBO() {
        return currentVBO;
    }
    
    /**
     * Render all orbital paths with distance-based visibility for planets
     */
    public void renderOrbits(Matrix4f mvpMatrix, float cameraX, float cameraY, float cameraZ) {
        glUseProgram(shaders.gridShaderProgram); // USE SAME SHADER AS GRID FOR THIN LINES
        
        // Set line width for better visibility
        glLineWidth(2.0f);
        
        FloatBuffer orbitMatrixBuffer = BufferUtils.createFloatBuffer(16);
        mvpMatrix.get(orbitMatrixBuffer);
        glUniformMatrix4fv(shaders.gridMvpLocation, false, orbitMatrixBuffer);
        
        // DISTANCE THRESHOLD FOR HIDING PLANET ORBITS (ADJUST AS NEEDED)
        float orbitHideDistance = 100.0f;
        
        // RENDER MERCURY ORBIT (HIDE WHEN CLOSE)
        if (getDistanceToPoint(cameraX, cameraY, cameraZ, sceneManager.getMercury().getPosition()) > orbitHideDistance) {
            glBindVertexArray(mercuryOrbitVAO);
            glDrawArrays(GL_LINE_LOOP, 0, ORBIT_SEGMENTS + 1);
        }
        
        // RENDER VENUS ORBIT (HIDE WHEN CLOSE)
        if (getDistanceToPoint(cameraX, cameraY, cameraZ, sceneManager.getVenus().getPosition()) > orbitHideDistance) {
            glBindVertexArray(venusOrbitVAO);
            glDrawArrays(GL_LINE_LOOP, 0, ORBIT_SEGMENTS + 1);
        }
        
        // RENDER EARTH ORBIT (HIDE WHEN CLOSE)
        if (getDistanceToPoint(cameraX, cameraY, cameraZ, sceneManager.getEarth().getPosition()) > orbitHideDistance) {
            glBindVertexArray(earthOrbitVAO);
            glDrawArrays(GL_LINE_LOOP, 0, ORBIT_SEGMENTS + 1);
        }
        
        // RENDER MOON ORBIT (ALWAYS VISIBLE - NOT AFFECTED BY DISTANCE)
        renderRelativeOrbit(moonOrbitVAO, sceneManager.getEarth().getPosition(), mvpMatrix);
        
        // RESET MATRIX FOR MARS ORBIT (SINCE renderRelativeOrbit MODIFIED THE UNIFORM)
        mvpMatrix.get(orbitMatrixBuffer);
        glUniformMatrix4fv(shaders.gridMvpLocation, false, orbitMatrixBuffer);
        
        // RENDER MARS ORBIT (HIDE WHEN CLOSE)
        if (getDistanceToPoint(cameraX, cameraY, cameraZ, sceneManager.getMars().getPosition()) > orbitHideDistance) {
            glBindVertexArray(marsOrbitVAO);
            glDrawArrays(GL_LINE_LOOP, 0, ORBIT_SEGMENTS + 1);
        }
        
        // RENDER PHOBOS ORBIT (ALWAYS VISIBLE - NOT AFFECTED BY DISTANCE)
        renderRelativeOrbit(phobosOrbitVAO, sceneManager.getMars().getPosition(), mvpMatrix);
        
        // RENDER DEIMOS ORBIT (ALWAYS VISIBLE - NOT AFFECTED BY DISTANCE)
        renderRelativeOrbit(deimosOrbitVAO, sceneManager.getMars().getPosition(), mvpMatrix);
        
        // RESET MATRIX FOR JUPITER ORBIT (SINCE renderRelativeOrbit MODIFIED THE UNIFORM)
        mvpMatrix.get(orbitMatrixBuffer);
        glUniformMatrix4fv(shaders.gridMvpLocation, false, orbitMatrixBuffer);
        
        // RENDER JUPITER ORBIT (HIDE WHEN CLOSE - LARGER HIDE DISTANCE DUE TO JUPITER'S SIZE)
        float jupiterHideDistance = 1000.0f; // Much larger hide distance for Jupiter
        if (getDistanceToPoint(cameraX, cameraY, cameraZ, sceneManager.getJupiter().getPosition()) > jupiterHideDistance) {
            glBindVertexArray(jupiterOrbitVAO);
            glDrawArrays(GL_LINE_LOOP, 0, ORBIT_SEGMENTS + 1);
        }
        
        // RENDER JUPITER MOON ORBITS (ALWAYS VISIBLE - NOT AFFECTED BY DISTANCE)
        renderRelativeOrbit(ioOrbitVAO, sceneManager.getJupiter().getPosition(), mvpMatrix);
        renderRelativeOrbit(europaOrbitVAO, sceneManager.getJupiter().getPosition(), mvpMatrix);
        renderRelativeOrbit(ganymedeOrbitVAO, sceneManager.getJupiter().getPosition(), mvpMatrix);
        renderRelativeOrbit(callistoOrbitVAO, sceneManager.getJupiter().getPosition(), mvpMatrix);
    }
    
    /**
     * Render an orbit relative to a parent object's position
     */
    private void renderRelativeOrbit(int orbitVAO, org.joml.Vector3f parentPosition, Matrix4f mvpMatrix) {
        Matrix4f orbitModel = new Matrix4f();
        orbitModel.translate(parentPosition); // TRANSLATE ORBIT TO PARENT'S POSITION
        Matrix4f orbitMVP = new Matrix4f(mvpMatrix);
        orbitMVP.mul(orbitModel); // CORRECT ORDER: MVP * Model
        
        FloatBuffer orbitMatrixBuffer = BufferUtils.createFloatBuffer(16);
        orbitMVP.get(orbitMatrixBuffer);
        glUniformMatrix4fv(shaders.gridMvpLocation, false, orbitMatrixBuffer);
        
        glBindVertexArray(orbitVAO);
        glDrawArrays(GL_LINE_LOOP, 0, ORBIT_SEGMENTS + 1);
    }
    
    /**
     * Create Jupiter's orbit around the Sun
     */
    private void createJupiterOrbit() {
        // Jupiter's distance from Sun: 778.5 million km (77850 units in our scale)
        float jupiterDistance = sceneManager.getJupiter().getDistanceFromSun();
        
        float[] jupiterOrbitVertices = new float[(ORBIT_SEGMENTS + 1) * 3];
        for (int i = 0; i <= ORBIT_SEGMENTS; i++) {
            float angle = (float) (2.0 * Math.PI * i / ORBIT_SEGMENTS);
            jupiterOrbitVertices[i * 3] = jupiterDistance * (float) Math.cos(angle);
            jupiterOrbitVertices[i * 3 + 1] = 0.0f;
            jupiterOrbitVertices[i * 3 + 2] = jupiterDistance * (float) Math.sin(angle);
        }
        
        jupiterOrbitVAO = glGenVertexArrays();
        jupiterOrbitVBO = glGenBuffers();
        
        glBindVertexArray(jupiterOrbitVAO);
        glBindBuffer(GL_ARRAY_BUFFER, jupiterOrbitVBO);
        
        FloatBuffer jupiterOrbitBuffer = BufferUtils.createFloatBuffer(jupiterOrbitVertices.length);
        jupiterOrbitBuffer.put(jupiterOrbitVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, jupiterOrbitBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    /**
     * Create Io's orbit around Jupiter
     */
    private void createIoOrbit() {
        // Io's distance from Jupiter: 421,600 km (42.16 units in our scale)
        // Scale up by 10x for better visibility when viewing Jupiter
        float ioDistance = 42.16f * 10.0f;
        
        float[] ioOrbitVertices = new float[(ORBIT_SEGMENTS + 1) * 3];
        for (int i = 0; i <= ORBIT_SEGMENTS; i++) {
            float angle = (float) (2.0 * Math.PI * i / ORBIT_SEGMENTS);
            ioOrbitVertices[i * 3] = ioDistance * (float) Math.cos(angle);
            ioOrbitVertices[i * 3 + 1] = 0.0f;
            ioOrbitVertices[i * 3 + 2] = ioDistance * (float) Math.sin(angle);
        }
        
        ioOrbitVAO = glGenVertexArrays();
        ioOrbitVBO = glGenBuffers();
        
        glBindVertexArray(ioOrbitVAO);
        glBindBuffer(GL_ARRAY_BUFFER, ioOrbitVBO);
        
        FloatBuffer ioOrbitBuffer = BufferUtils.createFloatBuffer(ioOrbitVertices.length);
        ioOrbitBuffer.put(ioOrbitVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, ioOrbitBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    /**
     * Create Europa's orbit around Jupiter
     */
    private void createEuropaOrbit() {
        // Europa's distance from Jupiter: 670,900 km (67.09 units in our scale)
        // Scale up by 10x for better visibility when viewing Jupiter
        float europaDistance = 67.09f * 10.0f;
        
        float[] europaOrbitVertices = new float[(ORBIT_SEGMENTS + 1) * 3];
        for (int i = 0; i <= ORBIT_SEGMENTS; i++) {
            float angle = (float) (2.0 * Math.PI * i / ORBIT_SEGMENTS);
            europaOrbitVertices[i * 3] = europaDistance * (float) Math.cos(angle);
            europaOrbitVertices[i * 3 + 1] = 0.0f;
            europaOrbitVertices[i * 3 + 2] = europaDistance * (float) Math.sin(angle);
        }
        
        europaOrbitVAO = glGenVertexArrays();
        europaOrbitVBO = glGenBuffers();
        
        glBindVertexArray(europaOrbitVAO);
        glBindBuffer(GL_ARRAY_BUFFER, europaOrbitVBO);
        
        FloatBuffer europaOrbitBuffer = BufferUtils.createFloatBuffer(europaOrbitVertices.length);
        europaOrbitBuffer.put(europaOrbitVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, europaOrbitBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    /**
     * Create Ganymede's orbit around Jupiter
     */
    private void createGanymedeOrbit() {
        // Ganymede's distance from Jupiter: 1,070,400 km (107.04 units in our scale)
        // Scale up by 10x for better visibility when viewing Jupiter
        float ganymedeDistance = 107.04f * 10.0f;
        
        float[] ganymedeOrbitVertices = new float[(ORBIT_SEGMENTS + 1) * 3];
        for (int i = 0; i <= ORBIT_SEGMENTS; i++) {
            float angle = (float) (2.0 * Math.PI * i / ORBIT_SEGMENTS);
            ganymedeOrbitVertices[i * 3] = ganymedeDistance * (float) Math.cos(angle);
            ganymedeOrbitVertices[i * 3 + 1] = 0.0f;
            ganymedeOrbitVertices[i * 3 + 2] = ganymedeDistance * (float) Math.sin(angle);
        }
        
        ganymedeOrbitVAO = glGenVertexArrays();
        ganymedeOrbitVBO = glGenBuffers();
        
        glBindVertexArray(ganymedeOrbitVAO);
        glBindBuffer(GL_ARRAY_BUFFER, ganymedeOrbitVBO);
        
        FloatBuffer ganymedeOrbitBuffer = BufferUtils.createFloatBuffer(ganymedeOrbitVertices.length);
        ganymedeOrbitBuffer.put(ganymedeOrbitVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, ganymedeOrbitBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    /**
     * Create Callisto's orbit around Jupiter
     */
    private void createCallistoOrbit() {
        // Callisto's distance from Jupiter: 1,882,000 km (188.2 units in our scale)
        // Scale up by 10x for better visibility when viewing Jupiter
        float callistoDistance = 188.2f * 10.0f;
        
        float[] callistoOrbitVertices = new float[(ORBIT_SEGMENTS + 1) * 3];
        for (int i = 0; i <= ORBIT_SEGMENTS; i++) {
            float angle = (float) (2.0 * Math.PI * i / ORBIT_SEGMENTS);
            callistoOrbitVertices[i * 3] = callistoDistance * (float) Math.cos(angle);
            callistoOrbitVertices[i * 3 + 1] = 0.0f;
            callistoOrbitVertices[i * 3 + 2] = callistoDistance * (float) Math.sin(angle);
        }
        
        callistoOrbitVAO = glGenVertexArrays();
        callistoOrbitVBO = glGenBuffers();
        
        glBindVertexArray(callistoOrbitVAO);
        glBindBuffer(GL_ARRAY_BUFFER, callistoOrbitVBO);
        
        FloatBuffer callistoOrbitBuffer = BufferUtils.createFloatBuffer(callistoOrbitVertices.length);
        callistoOrbitBuffer.put(callistoOrbitVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, callistoOrbitBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    /**
     * Clean up all orbital rendering resources
     */
    public void cleanup() {
        glDeleteVertexArrays(mercuryOrbitVAO);
        glDeleteBuffers(mercuryOrbitVBO);
        glDeleteVertexArrays(venusOrbitVAO);
        glDeleteBuffers(venusOrbitVBO);
        glDeleteVertexArrays(earthOrbitVAO);
        glDeleteBuffers(earthOrbitVBO);
        glDeleteVertexArrays(moonOrbitVAO);
        glDeleteBuffers(moonOrbitVBO);
        glDeleteVertexArrays(marsOrbitVAO);
        glDeleteBuffers(marsOrbitVBO);
        glDeleteVertexArrays(phobosOrbitVAO);
        glDeleteBuffers(phobosOrbitVBO);
        glDeleteVertexArrays(deimosOrbitVAO);
        glDeleteBuffers(deimosOrbitVBO);
        
        // Clean up Jupiter and its moon orbits
        glDeleteVertexArrays(jupiterOrbitVAO);
        glDeleteBuffers(jupiterOrbitVBO);
        glDeleteVertexArrays(ioOrbitVAO);
        glDeleteBuffers(ioOrbitVBO);
        glDeleteVertexArrays(europaOrbitVAO);
        glDeleteBuffers(europaOrbitVBO);
        glDeleteVertexArrays(ganymedeOrbitVAO);
        glDeleteBuffers(ganymedeOrbitVBO);
        glDeleteVertexArrays(callistoOrbitVAO);
        glDeleteBuffers(callistoOrbitVBO);
    }
    
    /**
     * Calculate distance from camera to a point
     */
    private float getDistanceToPoint(float cameraX, float cameraY, float cameraZ, org.joml.Vector3f point) {
        float deltaX = cameraX - point.x;
        float deltaY = cameraY - point.y;
        float deltaZ = cameraZ - point.z;
        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }
}
