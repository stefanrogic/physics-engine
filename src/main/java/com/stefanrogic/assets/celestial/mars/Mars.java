package com.stefanrogic.assets.celestial.mars;

import org.joml.Vector3f;
import com.stefanrogic.core.astronomy.AstronomicalCalculator;
import com.stefanrogic.assets.Sphere;

public class Mars {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO, VBO, EBO;
    
    // MARS DATA (SCALE: 1 UNIT = 10,000 KM)
    private static final float MARS_RADIUS = 0.339f; // 3,390 KM ACTUAL RADIUS (REALISTIC SCALE)
    private static final float DISTANCE_FROM_SUN = 22790.0f; // 227.9 MILLION KM ACTUAL DISTANCE
    private static final int SPHERE_DETAIL = 20; // GOOD DETAIL FOR MARS
    
    // ORBITAL DATA
    private static final float ORBITAL_PERIOD = 687.0f; // MARS ORBITAL PERIOD IN EARTH DAYS
    private float currentAngle; // CURRENT ORBITAL ANGLE IN RADIANS
    
    // ROTATION DATA
    private static final float ROTATION_PERIOD = 1.026f; // MARS ROTATION PERIOD IN EARTH DAYS (24.6 HOURS)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    public Mars() {
        // MARS CURRENT POSITION BASED ON REAL-TIME ASTRONOMICAL DATA
        this.currentAngle = (float) AstronomicalCalculator.getCurrentOrbitalAngle("MARS");
        updatePosition();
        this.color = new Vector3f(0.8f, 0.4f, 0.2f); // REDDISH MARS COLOR (IRON OXIDE)
        this.sphere = new Sphere(MARS_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    public Vector3f getColor() { return color; }
    public Vector3f getPosition() { return position; }
    public Sphere getSphere() { return sphere; }
    public float getRadius() { return MARS_RADIUS; }
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
