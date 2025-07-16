package com.stefanrogic.objects;

import org.joml.Vector3f;

public class Deimos {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO, VBO, EBO;
    
    // DEIMOS DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float DEIMOS_RADIUS = 0.0006f; // 6.2 KM ACTUAL RADIUS (EXTREMELY TINY, POTATO-SHAPED)
    private static final float DISTANCE_FROM_MARS = 2.35f; // 23,463 KM ACTUAL DISTANCE
    private static final int SPHERE_DETAIL = 6; // VERY LOW DETAIL FOR TINY MOON
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 1.26f; // DEIMOS ORBITAL PERIOD IN EARTH DAYS (30.3 HOURS)
    private static final float DEIMOS_TIME_SCALE = 0.05f; // SLOW DOWN DEIMOS MOTION SIGNIFICANTLY FOR OBSERVATION (5% SPEED)
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA (DEIMOS IS TIDALLY LOCKED)
    private static final float ROTATION_PERIOD = 1.26f; // ROTATION PERIOD EQUALS ORBITAL PERIOD
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    // REFERENCE TO MARS FOR ORBITAL CALCULATIONS
    private Mars mars;
    
    public Deimos(Mars mars) {
        this.mars = mars;
        // START DEIMOS AT A DIFFERENT ORBITAL POSITION FROM PHOBOS
        this.currentAngle = (float) Math.PI; // START AT 180 DEGREES
        updatePosition();
        this.color = new Vector3f(0.3f, 0.3f, 0.3f); // DARKER GRAY DEIMOS COLOR
        this.sphere = new Sphere(DEIMOS_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    public Vector3f getColor() { return color; }
    public Vector3f getPosition() { return position; }
    public Sphere getSphere() { return sphere; }
    public float getRadius() { return DEIMOS_RADIUS; }
    public float getDistanceFromMars() { return DISTANCE_FROM_MARS; }
    
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
        
        // APPLY DEIMOS TIME SCALE TO MOTION FOR BETTER OBSERVATION
        float scaledDeltaTime = deltaTime * DEIMOS_TIME_SCALE;
        
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
    
    // CALCULATE POSITION RELATIVE TO MARS
    private void updatePosition() {
        // GET MARS CURRENT POSITION
        Vector3f marsPos = mars.getPosition();
        
        // CALCULATE DEIMOS POSITION RELATIVE TO MARS
        float deimosX = marsPos.x + (float) (DISTANCE_FROM_MARS * Math.cos(currentAngle));
        float deimosY = marsPos.y; // DEIMOS ORBITS IN MARS ORBITAL PLANE (SIMPLIFIED)
        float deimosZ = marsPos.z + (float) (DISTANCE_FROM_MARS * Math.sin(currentAngle));
        
        this.position = new Vector3f(deimosX, deimosY, deimosZ);
    }
    
    // GETTERS FOR ORBITAL DATA
    public float getOrbitalPeriod() { return ORBITAL_PERIOD; }
    public float getCurrentAngle() { return currentAngle; }
    public float getRotationPeriod() { return ROTATION_PERIOD; }
    public float getRotationAngle() { return rotationAngle; }
}
