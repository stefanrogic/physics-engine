package com.stefanrogic.core.rendering;

import com.stefanrogic.core.scene.SceneManager;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.joml.Vector3f;
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
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
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
        createEarthBuffersWithSurface(earth);
    }
    
    private void createMoonBuffers() {
        Moon moon = sceneManager.getMoon();
        createMoonBuffersWithCraters(moon);
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
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // NORMAL ATTRIBUTE (LOCATION = 1) - FOR LIGHTING
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        // TEXTURE COORDINATE ATTRIBUTE (LOCATION = 2)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
        
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
    
    /**
     * Create Earth buffers with procedural surface (continents, oceans, ice caps)
     */
    private void createEarthBuffersWithSurface(Earth earth) {
        // CREATE VAO, VBO, EBO
        int VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
        
        earth.setVAO(VAO);
        earth.setVBO(VBO);
        earth.setEBO(EBO);
        
        glBindVertexArray(VAO);
        
        // GENERATE EARTH SURFACE WITH COLOR VARIATIONS
        float[] earthVertices = generateEarthSurfaceVertices(earth);
        
        // UPLOAD VERTEX DATA
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(earthVertices.length);
        vertexBuffer.put(earthVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        // UPLOAD INDEX DATA
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(earth.getSphere().getIndices().length);
        indexBuffer.put(earth.getSphere().getIndices()).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        
        // POSITION ATTRIBUTE (LOCATION = 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 11 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // NORMAL ATTRIBUTE (LOCATION = 1)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 11 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        // TEXTURE COORDINATE ATTRIBUTE (LOCATION = 2)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 11 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
        
        // SURFACE COLOR ATTRIBUTE (LOCATION = 3)
        glVertexAttribPointer(3, 3, GL_FLOAT, false, 11 * Float.BYTES, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);
        
        glBindVertexArray(0);
    }
    
    /**
     * Create Moon buffers with procedural craters and surface features
     */
    private void createMoonBuffersWithCraters(Moon moon) {
        // CREATE VAO, VBO, EBO
        int VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
        
        moon.setVAO(VAO);
        moon.setVBO(VBO);
        moon.setEBO(EBO);
        
        glBindVertexArray(VAO);
        
        // GENERATE MOON SURFACE WITH CRATER PATTERNS
        float[] moonVertices = generateMoonSurfaceVertices(moon);
        
        // UPLOAD VERTEX DATA
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(moonVertices.length);
        vertexBuffer.put(moonVertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        // UPLOAD INDEX DATA
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(moon.getSphere().getIndices().length);
        indexBuffer.put(moon.getSphere().getIndices()).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        
        // POSITION ATTRIBUTE (LOCATION = 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 11 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // NORMAL ATTRIBUTE (LOCATION = 1)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 11 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        // TEXTURE COORDINATE ATTRIBUTE (LOCATION = 2)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 11 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
        
        // SURFACE COLOR ATTRIBUTE (LOCATION = 3)
        glVertexAttribPointer(3, 3, GL_FLOAT, false, 11 * Float.BYTES, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);
        
        glBindVertexArray(0);
    }
    
    /**
     * Generate Earth surface vertices with continent/ocean color patterns
     */
    private float[] generateEarthSurfaceVertices(Earth earth) {
        List<Float> vertexList = new ArrayList<>();
        Sphere sphere = earth.getSphere();
        
        int latSegs = 24; // EARTH DETAIL LEVEL
        int lonSegs = 24;
        
        for (int lat = 0; lat <= latSegs; lat++) {
            float theta = (float) (lat * Math.PI / latSegs);
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);
            
            for (int lon = 0; lon <= lonSegs; lon++) {
                float phi = (float) (lon * 2 * Math.PI / lonSegs);
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);
                
                float x = cosPhi * sinTheta;
                float y = cosTheta;
                float z = sinPhi * sinTheta;
                
                // POSITION
                vertexList.add(x * earth.getRadius());
                vertexList.add(y * earth.getRadius());
                vertexList.add(z * earth.getRadius());
                
                // NORMAL
                vertexList.add(x);
                vertexList.add(y);
                vertexList.add(z);
                
                // TEXTURE COORDINATES
                float u = (float) lon / lonSegs;
                float v = (float) lat / latSegs;
                vertexList.add(u);
                vertexList.add(v);
                
                // SURFACE COLOR - EARTH PATTERNS
                Vector3f surfaceColor = getEarthSurfaceColor(theta - (float)Math.PI/2, phi - (float)Math.PI, earth);
                vertexList.add(surfaceColor.x);
                vertexList.add(surfaceColor.y);
                vertexList.add(surfaceColor.z);
            }
        }
        
        return convertToFloatArray(vertexList);
    }
    
    /**
     * Generate Moon surface vertices with crater patterns
     */
    private float[] generateMoonSurfaceVertices(Moon moon) {
        List<Float> vertexList = new ArrayList<>();
        
        int latSegs = 16; // MOON DETAIL LEVEL
        int lonSegs = 16;
        
        for (int lat = 0; lat <= latSegs; lat++) {
            float theta = (float) (lat * Math.PI / latSegs);
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);
            
            for (int lon = 0; lon <= lonSegs; lon++) {
                float phi = (float) (lon * 2 * Math.PI / lonSegs);
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);
                
                float x = cosPhi * sinTheta;
                float y = cosTheta;
                float z = sinPhi * sinTheta;
                
                // POSITION
                vertexList.add(x * moon.getRadius());
                vertexList.add(y * moon.getRadius());
                vertexList.add(z * moon.getRadius());
                
                // NORMAL
                vertexList.add(x);
                vertexList.add(y);
                vertexList.add(z);
                
                // TEXTURE COORDINATES
                float u = (float) lon / lonSegs;
                float v = (float) lat / latSegs;
                vertexList.add(u);
                vertexList.add(v);
                
                // SURFACE COLOR - MOON CRATERS
                Vector3f surfaceColor = getMoonSurfaceColor(theta - (float)Math.PI/2, phi - (float)Math.PI, moon);
                vertexList.add(surfaceColor.x);
                vertexList.add(surfaceColor.y);
                vertexList.add(surfaceColor.z);
            }
        }
        
        return convertToFloatArray(vertexList);
    }
    
    /**
     * Get Earth surface color based on latitude/longitude (continents, oceans, ice)
     */
    private Vector3f getEarthSurfaceColor(float latitude, float longitude, Earth earth) {
        float lat = (latitude + (float)Math.PI/2) / (float)Math.PI; // 0 to 1
        float lon = (longitude + (float)Math.PI) / (2*(float)Math.PI); // 0 to 1
        
        // ICE CAPS AT POLES
        if (lat < 0.08f || lat > 0.92f) {
            return earth.getIceColor();
        }
        
        // CONTINENT PATTERNS
        boolean isLand = false;
        
        // AFRICA AND EUROPE (longitude 0.0-0.25, latitude 0.3-0.8)
        if (lon >= 0.0f && lon <= 0.25f && lat >= 0.3f && lat <= 0.8f) {
            isLand = true;
        }
        // ASIA (longitude 0.2-0.7, latitude 0.45-0.85)
        else if (lon >= 0.2f && lon <= 0.7f && lat >= 0.45f && lat <= 0.85f) {
            isLand = true;
        }
        // NORTH AMERICA (longitude 0.75-1.0, latitude 0.5-0.85)
        else if (lon >= 0.75f && lat >= 0.5f && lat <= 0.85f) {
            isLand = true;
        }
        // SOUTH AMERICA (longitude 0.8-0.95, latitude 0.15-0.55)
        else if (lon >= 0.8f && lon <= 0.95f && lat >= 0.15f && lat <= 0.55f) {
            isLand = true;
        }
        // AUSTRALIA (longitude 0.6-0.75, latitude 0.15-0.35)
        else if (lon >= 0.6f && lon <= 0.75f && lat >= 0.15f && lat <= 0.35f) {
            isLand = true;
        }
        
        // ADD COASTLINE VARIATION
        float noise = (float)(Math.sin(lat * 25) * Math.cos(lon * 30) * 0.06);
        
        if (isLand) {
            // LAND COLOR WITH LATITUDE VARIATION
            float greenness = 1.0f - Math.abs(lat - 0.5f) * 1.5f;
            greenness = Math.max(0.3f, Math.min(1.0f, greenness));
            
            Vector3f landColor = earth.getLandColor();
            Vector3f mountainColor = earth.getMountainColor();
            
            return new Vector3f(
                landColor.x + (mountainColor.x - landColor.x) * (1.0f - greenness) + noise * 0.3f,
                landColor.y * greenness + noise * 0.2f,
                landColor.z + noise * 0.1f
            );
        } else {
            // OCEAN COLOR WITH DEPTH VARIATION
            float depth = 0.8f + noise * 0.2f;
            Vector3f oceanColor = earth.getOceanColor();
            
            return new Vector3f(
                oceanColor.x * depth,
                oceanColor.y * depth,
                oceanColor.z * Math.max(0.7f, depth)
            );
        }
    }
    
    /**
     * Get Moon surface color with crater patterns
     */
    private Vector3f getMoonSurfaceColor(float latitude, float longitude, Moon moon) {
        float lat = (latitude + (float)Math.PI/2) / (float)Math.PI;
        float lon = (longitude + (float)Math.PI) / (2*(float)Math.PI);
        
        Vector3f baseColor = moon.getColor();
        Vector3f result = new Vector3f(baseColor);
        
        // MAJOR CRATER LOCATIONS (FAMOUS LUNAR CRATERS)
        float[][] craters = {
            {0.7f, 0.3f, 0.12f},  // TYCHO (PROMINENT SOUTHERN CRATER)
            {0.35f, 0.7f, 0.10f}, // COPERNICUS (VISIBLE WITH NAKED EYE)
            {0.85f, 0.5f, 0.15f}, // CLAVIUS (LARGE SOUTHERN CRATER)
            {0.25f, 0.4f, 0.08f}, // ARISTARCHUS (BRIGHT CRATER)
            {0.6f, 0.8f, 0.06f},  // PLATO (DARK FLOORED CRATER)
            {0.5f, 0.6f, 0.11f},  // PTOLEMAEUS (LARGE CENTRAL CRATER)
            {0.15f, 0.2f, 0.07f}, // LANGRENUS (EASTERN CRATER)
            {0.9f, 0.4f, 0.09f}   // PETAVIUS (SOUTHEASTERN CRATER)
        };
        
        float craterEffect = 0.0f;
        for (float[] crater : craters) {
            float deltaLat = lat - crater[0];
            float deltaLon = lon - crater[1];
            
            // HANDLE LONGITUDE WRAPAROUND
            if (deltaLon > 0.5f) deltaLon -= 1.0f;
            if (deltaLon < -0.5f) deltaLon += 1.0f;
            
            float distance = (float)Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon);
            
            if (distance < crater[2]) {
                float depthFactor = 1.0f - (distance / crater[2]);
                float rimEffect = (float)Math.sin(depthFactor * Math.PI);
                
                if (distance < crater[2] * 0.6f) {
                    // CRATER INTERIOR - MUCH DARKER
                    craterEffect -= rimEffect * 0.35f;
                } else {
                    // CRATER RIM - BRIGHTER FROM IMPACT EJECTA
                    craterEffect += rimEffect * 0.2f;
                }
            }
        }
        
        // MARIA (DARK VOLCANIC PLAINS)
        float maria = 0.0f;
        if ((lat >= 0.4f && lat <= 0.7f && lon >= 0.1f && lon <= 0.4f) || // MARE TRANQUILLITATIS
            (lat >= 0.3f && lat <= 0.6f && lon >= 0.5f && lon <= 0.8f) || // MARE IMBRIUM
            (lat >= 0.2f && lat <= 0.5f && lon >= 0.2f && lon <= 0.6f)) { // MARE SERENITATIS
            maria = -0.25f; // DARKER BASALTIC ROCK
        }
        
        // GENERAL SURFACE ROUGHNESS
        float roughness = (float)(
            Math.sin(lat * 40) * Math.cos(lon * 50) * 0.04 +
            Math.sin(lat * 80) * Math.cos(lon * 90) * 0.02
        );
        
        float totalEffect = craterEffect + maria + roughness;
        totalEffect = Math.max(-0.5f, Math.min(0.3f, totalEffect));
        
        return new Vector3f(
            Math.max(0.1f, result.x + totalEffect),
            Math.max(0.1f, result.y + totalEffect),
            Math.max(0.1f, result.z + totalEffect * 0.9f)
        );
    }
    
    /**
     * Convert ArrayList<Float> to float array
     */
    private float[] convertToFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
