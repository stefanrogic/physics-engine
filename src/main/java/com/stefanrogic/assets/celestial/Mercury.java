package com.stefanrogic.assets.celestial;

import org.joml.Vector3f;
import com.stefanrogic.core.astronomy.AstronomicalCalculator;
import com.stefanrogic.assets.Sphere;

public class Mercury {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO, VBO, EBO;
    
    // MERCURY DATA (SCALE: 1 UNIT = 10,000 KM) - REALISTIC DISTANCE
    private static final float MERCURY_RADIUS = 0.24f; // 2,440 KM ACTUAL RADIUS
    private static final float DISTANCE_FROM_SUN = 5800.0f; // 58 MILLION KM ACTUAL DISTANCE (FROM SUN CENTER)
    private static final int SPHERE_DETAIL = 16; // LOWER DETAIL FOR SMALL PLANET
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 88.0f; // MERCURY'S ORBITAL PERIOD IN EARTH DAYS
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA
    private static final float ROTATION_PERIOD = 58.6f; // MERCURY'S ROTATION PERIOD IN EARTH DAYS (SLOW ROTATION)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    public Mercury() {
        // MERCURY'S CURRENT POSITION BASED ON REAL-TIME ASTRONOMICAL DATA
        this.currentAngle = (float) AstronomicalCalculator.getCurrentOrbitalAngle("MERCURY");
        updatePosition();
        this.color = new Vector3f(0.8f, 0.7f, 0.6f); // GRAYISH-BROWN MERCURY COLOR
        this.sphere = new Sphere(MERCURY_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    public Vector3f getPosition() { return position; }
    public Vector3f getColor() { return color; }
    public Sphere getSphere() { return sphere; }
    public float getRadius() { return MERCURY_RADIUS; }
    public float getDistanceFromSun() { return DISTANCE_FROM_SUN; }
    
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
        
        // CALCULATE ROTATIONAL ANGULAR VELOCITY (RADIANS PER SECOND)
        float rotationalAngularVelocity = (float) (2.0 * Math.PI) / (ROTATION_PERIOD * 24.0f * 3600.0f); // CONVERT DAYS TO SECONDS
        
        // UPDATE ORBITAL ANGLE
        currentAngle += orbitalAngularVelocity * deltaTime;
        
        // UPDATE ROTATION ANGLE
        rotationAngle += rotationalAngularVelocity * deltaTime;
        
        // KEEP ANGLES IN 0-2π RANGE
        if (currentAngle > 2.0 * Math.PI) {
            currentAngle -= (float) (2.0 * Math.PI);
        }
        if (rotationAngle > 2.0 * Math.PI) {
            rotationAngle -= (float) (2.0 * Math.PI);
        }
        
        updatePosition();
    }
    
    // CALCULATE POSITION FROM CURRENT ANGLE
    private void updatePosition() {
        float x = (float) (DISTANCE_FROM_SUN * Math.cos(currentAngle));
        float z = (float) (DISTANCE_FROM_SUN * Math.sin(currentAngle));
        this.position = new Vector3f(x, 0.0f, z);
    }
    
    // GETTERS FOR ORBITAL DATA
    public float getRotationAngle() { return rotationAngle; }
}
