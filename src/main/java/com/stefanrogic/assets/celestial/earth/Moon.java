package com.stefanrogic.assets.celestial.earth;

import org.joml.Vector3f;
import com.stefanrogic.assets.Sphere;

public class Moon {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO, VBO, EBO;
    
    // MOON DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float MOON_RADIUS = 0.174f; // 1,737 KM ACTUAL RADIUS (REALISTIC SCALE)
    private static final float DISTANCE_FROM_EARTH = 38.0f; // 384,400 KM ACTUAL DISTANCE (SCALED DOWN FOR VISIBILITY)
    private static final int SPHERE_DETAIL = 16; // MEDIUM DETAIL FOR MOON
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 27.3f; // MOON'S ORBITAL PERIOD IN EARTH DAYS
    private static final float MOON_TIME_SCALE = 0.1f; // SLOW DOWN MOON'S MOTION FOR BETTER OBSERVATION (10% SPEED)
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA (MOON IS TIDALLY LOCKED - SAME FACE ALWAYS TOWARD EARTH)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    // REFERENCE TO EARTH FOR ORBITAL CALCULATIONS
    private Earth earth;
    
    public Moon(Earth earth) {
        this.earth = earth;
        // START MOON AT A RANDOM ORBITAL POSITION
        this.currentAngle = 0.0f; // START AT 0 DEGREES
        updatePosition();
        this.color = new Vector3f(0.6f, 0.6f, 0.65f); // GRAYISH MOON WITH SLIGHT BLUE TINT
        this.sphere = new Sphere(MOON_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    public Vector3f getColor() { return color; }
    
    /**
     * Get enhanced Moon color with crater variations
     */
    public Vector3f getEnhancedColor() {
        // SIMULATE LUNA SURFACE WITH MARIA (DARK AREAS) AND HIGHLANDS
        return new Vector3f(
            color.x * 0.8f, // DARKER THAN PURE GRAY
            color.y * 0.8f,
            color.z * 0.85f // SLIGHT BLUE TINT FROM EARTHLIGHT
        );
    }
    
    /**
     * GET COLOR WITH CRATER PATTERN TO SIMULATE MOON'S SURFACE
     * This method creates crater patterns based on spherical coordinates
     */
    public Vector3f getColorAtPosition(float latitude, float longitude) {
        // NORMALIZE COORDINATES
        float lat = (latitude + (float)Math.PI/2) / (float)Math.PI; // 0 to 1
        float lon = (longitude + (float)Math.PI) / (2*(float)Math.PI); // 0 to 1
        
        Vector3f baseColor = new Vector3f(color);
        
        // CREATE CRATER PATTERN USING MATHEMATICAL FUNCTIONS
        float craterPattern = 0.0f;
        
        // MAJOR CRATER CENTERS (APPROXIMATING REAL LUNAR FEATURES)
        float[][] craters = {
            {0.6f, 0.3f, 0.15f}, // TYCHO CRATER (SOUTH)
            {0.4f, 0.7f, 0.12f}, // COPERNICUS (NORTH)
            {0.8f, 0.5f, 0.18f}, // CLAVIUS (SOUTH)
            {0.3f, 0.4f, 0.1f},  // ARISTARCHUS
            {0.7f, 0.8f, 0.08f}, // PLATO
            {0.5f, 0.6f, 0.14f}, // PTOLEMAEUS
            {0.2f, 0.2f, 0.09f}, // LANGRENUS
            {0.9f, 0.4f, 0.11f}, // PETAVIUS
            {0.45f, 0.35f, 0.13f}, // ALPHONSUS
            {0.65f, 0.65f, 0.07f}  // ARCHIMEDES
        };
        
        for (float[] crater : craters) {
            float centerLat = crater[0];
            float centerLon = crater[1];
            float radius = crater[2];
            
            // CALCULATE DISTANCE FROM CRATER CENTER
            float deltaLat = lat - centerLat;
            float deltaLon = lon - centerLon;
            
            // HANDLE LONGITUDE WRAPAROUND
            if (deltaLon > 0.5f) deltaLon -= 1.0f;
            if (deltaLon < -0.5f) deltaLon += 1.0f;
            
            float distance = (float)Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon);
            
            if (distance < radius) {
                // CRATER RIM AND INTERIOR
                float depthFactor = 1.0f - (distance / radius);
                float rimEffect = (float)Math.sin(depthFactor * Math.PI);
                
                if (distance < radius * 0.8f) {
                    // CRATER INTERIOR - DARKER
                    craterPattern += rimEffect * 0.4f;
                } else {
                    // CRATER RIM - BRIGHTER
                    craterPattern -= rimEffect * 0.2f;
                }
            }
        }
        
        // ADD GENERAL SURFACE ROUGHNESS
        float roughness = (float)(
            Math.sin(lat * 40) * Math.cos(lon * 50) * 0.05 +
            Math.sin(lat * 80) * Math.cos(lon * 90) * 0.03 +
            Math.sin(lat * 160) * Math.cos(lon * 180) * 0.02
        );
        
        // MARIA (DARK PLAINS) - SIMPLIFIED PATTERN
        float maria = 0.0f;
        if ((lat >= 0.4f && lat <= 0.7f && lon >= 0.1f && lon <= 0.4f) || // MARE TRANQUILLITATIS
            (lat >= 0.3f && lat <= 0.6f && lon >= 0.5f && lon <= 0.8f) || // MARE IMBRIUM
            (lat >= 0.2f && lat <= 0.5f && lon >= 0.2f && lon <= 0.6f)) { // MARE SERENITATIS
            maria = -0.3f;
        }
        
        // COMBINE ALL EFFECTS
        float totalEffect = craterPattern + roughness + maria;
        totalEffect = Math.max(-0.5f, Math.min(0.3f, totalEffect)); // CLAMP
        
        return new Vector3f(
            Math.max(0.1f, baseColor.x + totalEffect),
            Math.max(0.1f, baseColor.y + totalEffect),
            Math.max(0.1f, baseColor.z + totalEffect)
        );
    }
    public Vector3f getPosition() { return position; }
    public Sphere getSphere() { return sphere; }
    public float getRadius() { return MOON_RADIUS; }
    public float getDistanceFromEarth() { return DISTANCE_FROM_EARTH; }
    
    // GETTERS FOR OPENGL BUFFER OBJECTS
    public int getVAO() { return VAO; }
    public int getVBO() { return VBO; }
    public int getEBO() { return EBO; }
    
    // SETTERS FOR BUFFER OBJECTS (TO BE SET BY RENDERER)
    public void setVAO(int VAO) { this.VAO = VAO; }
    public void setVBO(int VBO) { this.VBO = VBO; }
    public void setEBO(int EBO) { this.EBO = EBO; }
    
    // UPDATE ORBITAL POSITION AND ROTATION BASED ON TIME
    public void updateOrbitalPosition(float deltaTime) {
        // CALCULATE ORBITAL ANGULAR VELOCITY (RADIANS PER SECOND)
        float orbitalAngularVelocity = (float) (2.0 * Math.PI) / (ORBITAL_PERIOD * 24.0f * 3600.0f); // CONVERT DAYS TO SECONDS
        
        // APPLY MOON TIME SCALE TO ORBITAL MOTION FOR BETTER OBSERVATION
        float scaledDeltaTime = deltaTime * MOON_TIME_SCALE;
        
        // UPDATE ORBITAL ANGLE (WITH TIME SCALING)
        currentAngle += orbitalAngularVelocity * scaledDeltaTime;
        
        // TIDAL LOCKING: ROTATION ANGLE EXACTLY MATCHES ORBITAL ANGLE
        rotationAngle = currentAngle;
        
        // KEEP ANGLES IN 0-2π RANGE
        if (currentAngle > 2.0 * Math.PI) {
            currentAngle -= (float) (2.0 * Math.PI);
        }
        if (rotationAngle > 2.0 * Math.PI) {
            rotationAngle -= (float) (2.0 * Math.PI);
        }
        
        updatePosition();
    }
    
    // CALCULATE POSITION RELATIVE TO EARTH
    private void updatePosition() {
        // GET EARTH'S CURRENT POSITION
        Vector3f earthPos = earth.getPosition();
        
        // CALCULATE MOON'S POSITION RELATIVE TO EARTH
        float moonX = earthPos.x + (float) (DISTANCE_FROM_EARTH * Math.cos(currentAngle));
        float moonY = earthPos.y; // MOON ORBITS IN EARTH'S ORBITAL PLANE (SIMPLIFIED)
        float moonZ = earthPos.z + (float) (DISTANCE_FROM_EARTH * Math.sin(currentAngle));
        
        this.position = new Vector3f(moonX, moonY, moonZ);
    }
    
    // GETTERS FOR ORBITAL DATA
    public float getRotationAngle() { return rotationAngle; }
}
