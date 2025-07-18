package com.stefanrogic.assets.celestial.mars;

import org.joml.Vector3f;
import com.stefanrogic.assets.Sphere;

public class Phobos {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO, VBO, EBO;
    
    // PHOBOS DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float PHOBOS_RADIUS = 0.0011f; // 11.1 KM ACTUAL RADIUS (VERY TINY, POTATO-SHAPED)
    private static final float DISTANCE_FROM_MARS = 0.94f; // 9,376 KM ACTUAL DISTANCE
    private static final int SPHERE_DETAIL = 8; // LOW DETAIL FOR TINY MOON
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 0.32f; // PHOBOS ORBITAL PERIOD IN EARTH DAYS (7.6 HOURS)
    private static final float PHOBOS_TIME_SCALE = 0.02f; // SLOW DOWN PHOBOS MOTION SIGNIFICANTLY FOR OBSERVATION (2% SPEED)
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA (PHOBOS IS TIDALLY LOCKED - SAME FACE ALWAYS TOWARD MARS)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    // REFERENCE TO MARS FOR ORBITAL CALCULATIONS
    private Mars mars;
    
    public Phobos(Mars mars) {
        this.mars = mars;
        // START PHOBOS AT A RANDOM ORBITAL POSITION
        this.currentAngle = 0.0f; // START AT 0 DEGREES
        updatePosition();
        this.color = new Vector3f(0.4f, 0.4f, 0.4f); // DARK GRAY PHOBOS COLOR
        this.sphere = new Sphere(PHOBOS_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    public Vector3f getColor() { return color; }
    public Vector3f getPosition() { return position; }
    public Sphere getSphere() { return sphere; }
    public float getRadius() { return PHOBOS_RADIUS; }
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
        
        // APPLY PHOBOS TIME SCALE TO MOTION FOR BETTER OBSERVATION
        float scaledDeltaTime = deltaTime * PHOBOS_TIME_SCALE;
        
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
        
        // CALCULATE PHOBOS POSITION RELATIVE TO MARS
        float phobosX = marsPos.x + (float) (DISTANCE_FROM_MARS * Math.cos(currentAngle));
        float phobosY = marsPos.y; // PHOBOS ORBITS IN MARS ORBITAL PLANE (SIMPLIFIED)
        float phobosZ = marsPos.z + (float) (DISTANCE_FROM_MARS * Math.sin(currentAngle));
        
        this.position = new Vector3f(phobosX, phobosY, phobosZ);
    }
    
    // GETTERS FOR ORBITAL DATA
    public float getRotationAngle() { return rotationAngle; }
}
