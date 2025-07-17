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
    private static final float ROTATION_PERIOD = 27.3f; // MOON'S ROTATION PERIOD EQUALS ORBITAL PERIOD
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    // REFERENCE TO EARTH FOR ORBITAL CALCULATIONS
    private Earth earth;
    
    public Moon(Earth earth) {
        this.earth = earth;
        // START MOON AT A RANDOM ORBITAL POSITION
        this.currentAngle = 0.0f; // START AT 0 DEGREES
        updatePosition();
        this.color = new Vector3f(0.8f, 0.8f, 0.8f); // GRAY MOON COLOR
        this.sphere = new Sphere(MOON_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    public Vector3f getColor() { return color; }
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
    public float getOrbitalPeriod() { return ORBITAL_PERIOD; }
    public float getCurrentAngle() { return currentAngle; }
    public float getRotationPeriod() { return ROTATION_PERIOD; }
    public float getRotationAngle() { return rotationAngle; }
}
