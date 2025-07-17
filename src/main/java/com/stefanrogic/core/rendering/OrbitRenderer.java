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
