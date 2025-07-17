package com.stefanrogic.core.rendering;

import com.stefanrogic.core.scene.SceneManager;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import com.stefanrogic.assets.*;
import com.stefanrogic.assets.celestial.*;
import com.stefanrogic.assets.celestial.earth.*;
import com.stefanrogic.assets.celestial.mars.*;

/**
 * Manages OpenGL buffer creation and setup for all celestial objects
 */
public class ObjectRenderer {
    
    private SceneManager sceneManager;
    
    public ObjectRenderer(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }
    
    /**
     * Initialize OpenGL buffers for all celestial objects
     */
    public void initializeBuffers() {
        createSunBuffers();
        createMercuryBuffers();
        createVenusBuffers();
        createEarthBuffers();
        createMoonBuffers();
        createMarsBuffers();
        createPhobosBuffers();
        createDeimosBuffers();
    }
    
    private void createSunBuffers() {
        Sun sun = sceneManager.getSun();
        
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
    
    private void createMercuryBuffers() {
        Mercury mercury = sceneManager.getMercury();
        createPlanetBuffers(mercury, mercury.getSphere());
    }
    
    private void createVenusBuffers() {
        Venus venus = sceneManager.getVenus();
        createPlanetBuffers(venus, venus.getSphere());
    }
    
    private void createEarthBuffers() {
        Earth earth = sceneManager.getEarth();
        createPlanetBuffers(earth, earth.getSphere());
    }
    
    private void createMoonBuffers() {
        Moon moon = sceneManager.getMoon();
        createPlanetBuffers(moon, moon.getSphere());
    }
    
    private void createMarsBuffers() {
        Mars mars = sceneManager.getMars();
        createPlanetBuffers(mars, mars.getSphere());
    }
    
    private void createPhobosBuffers() {
        Phobos phobos = sceneManager.getPhobos();
        createPlanetBuffers(phobos, phobos.getSphere());
    }
    
    private void createDeimosBuffers() {
        Deimos deimos = sceneManager.getDeimos();
        createPlanetBuffers(deimos, deimos.getSphere());
    }
    
    /**
     * Generic method to create buffers for any celestial object with lighting support
     */
    private void createPlanetBuffers(Object planet, Sphere sphere) {
        // CREATE VAO, VBO, EBO
        int VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
        
        // Set buffers using reflection to handle different object types
        setPlanetBuffers(planet, VAO, VBO, EBO);
        
        glBindVertexArray(VAO);
        
        // UPLOAD VERTEX DATA
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(sphere.getVertices().length);
        vertexBuffer.put(sphere.getVertices()).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        // UPLOAD INDEX DATA
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(sphere.getIndices().length);
        indexBuffer.put(sphere.getIndices()).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        
        // POSITION ATTRIBUTE (LOCATION = 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // NORMAL ATTRIBUTE (LOCATION = 1) - FOR LIGHTING
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        glBindVertexArray(0);
    }
    
    /**
     * Set VAO, VBO, EBO for different planet types using method calls
     */
    private void setPlanetBuffers(Object planet, int VAO, int VBO, int EBO) {
        if (planet instanceof Mercury mercury) {
            mercury.setVAO(VAO);
            mercury.setVBO(VBO);
            mercury.setEBO(EBO);
        } else if (planet instanceof Venus venus) {
            venus.setVAO(VAO);
            venus.setVBO(VBO);
            venus.setEBO(EBO);
        } else if (planet instanceof Earth earth) {
            earth.setVAO(VAO);
            earth.setVBO(VBO);
            earth.setEBO(EBO);
        } else if (planet instanceof Moon moon) {
            moon.setVAO(VAO);
            moon.setVBO(VBO);
            moon.setEBO(EBO);
        } else if (planet instanceof Mars mars) {
            mars.setVAO(VAO);
            mars.setVBO(VBO);
            mars.setEBO(EBO);
        } else if (planet instanceof Phobos phobos) {
            phobos.setVAO(VAO);
            phobos.setVBO(VBO);
            phobos.setEBO(EBO);
        } else if (planet instanceof Deimos deimos) {
            deimos.setVAO(VAO);
            deimos.setVBO(VBO);
            deimos.setEBO(EBO);
        }
    }
    
    /**
     * Clean up all OpenGL resources
     */
    public void cleanup() {
        // Clean up celestial object buffers
        cleanupObjectBuffers(sceneManager.getSun());
        cleanupObjectBuffers(sceneManager.getMercury());
        cleanupObjectBuffers(sceneManager.getVenus());
        cleanupObjectBuffers(sceneManager.getEarth());
        cleanupObjectBuffers(sceneManager.getMoon());
        cleanupObjectBuffers(sceneManager.getMars());
        cleanupObjectBuffers(sceneManager.getPhobos());
        cleanupObjectBuffers(sceneManager.getDeimos());
    }
    
    private void cleanupObjectBuffers(Object object) {
        try {
            if (object instanceof Sun sun) {
                glDeleteVertexArrays(sun.getVAO());
                glDeleteBuffers(sun.getVBO());
                glDeleteBuffers(sun.getEBO());
            } else if (object instanceof Mercury mercury) {
                glDeleteVertexArrays(mercury.getVAO());
                glDeleteBuffers(mercury.getVBO());
                glDeleteBuffers(mercury.getEBO());
            } else if (object instanceof Venus venus) {
                glDeleteVertexArrays(venus.getVAO());
                glDeleteBuffers(venus.getVBO());
                glDeleteBuffers(venus.getEBO());
            } else if (object instanceof Earth earth) {
                glDeleteVertexArrays(earth.getVAO());
                glDeleteBuffers(earth.getVBO());
                glDeleteBuffers(earth.getEBO());
            } else if (object instanceof Moon moon) {
                glDeleteVertexArrays(moon.getVAO());
                glDeleteBuffers(moon.getVBO());
                glDeleteBuffers(moon.getEBO());
            } else if (object instanceof Mars mars) {
                glDeleteVertexArrays(mars.getVAO());
                glDeleteBuffers(mars.getVBO());
                glDeleteBuffers(mars.getEBO());
            } else if (object instanceof Phobos phobos) {
                glDeleteVertexArrays(phobos.getVAO());
                glDeleteBuffers(phobos.getVBO());
                glDeleteBuffers(phobos.getEBO());
            } else if (object instanceof Deimos deimos) {
                glDeleteVertexArrays(deimos.getVAO());
                glDeleteBuffers(deimos.getVBO());
                glDeleteBuffers(deimos.getEBO());
            }
        } catch (Exception e) {
            // Ignore cleanup errors - buffers may already be deleted
        }
    }
}
