package com.stefanrogic.assets.celestial;

import org.joml.Vector3f;
import com.stefanrogic.assets.Sphere;

public class Sun {
    private Sphere sphere;
    private Vector3f position;
    private Vector3f color;
    private int VAO, VBO, EBO;
    
    // SCALE: 1 UNIT = 10,000 KM (SO SUN RADIUS OF ~70 UNITS REPRESENTS ~696,000 KM ACTUAL RADIUS)
    private static final float SUN_RADIUS = 69.6f; // SCALED FOR BETTER PLANET VISIBILITY
    private static final int SPHERE_DETAIL = 32; // GOOD BALANCE OF DETAIL VS PERFORMANCE
    
    // ROTATION DATA
    private static final float ROTATION_PERIOD = 27.0f; // SUN'S ROTATION PERIOD IN EARTH DAYS (AT EQUATOR)
    private float rotationAngle; // CURRENT ROTATION ANGLE IN RADIANS
    
    public Sun() {
        this.position = new Vector3f(0.0f, 0.0f, 0.0f); // CENTER OF THE WORLD
        this.color = new Vector3f(1.0f, 0.8f, 0.0f); // YELLOW-ORANGE SUN COLOR
        this.sphere = new Sphere(SUN_RADIUS, SPHERE_DETAIL, SPHERE_DETAIL);
        this.rotationAngle = 0.0f; // START WITH NO ROTATION
        
        setupBuffers();
    }
    
    private void setupBuffers() {
        // THIS WILL BE CALLED FROM THE RENDERER TO SET UP OPENGL BUFFERS
        // WE'LL IMPLEMENT THIS IN THE RENDERER CLASS
    }
    
    public Vector3f getPosition() { return position; }
    public Vector3f getColor() { return color; }
    public Sphere getSphere() { return sphere; }
    public float getRadius() { return SUN_RADIUS; }
    
    // GETTERS FOR OPENGL BUFFER OBJECTS
    public int getVAO() { return VAO; }
    public int getVBO() { return VBO; }
    public int getEBO() { return EBO; }
    
    // SETTERS FOR BUFFER OBJECTS (TO BE SET BY RENDERER)
    public void setVAO(int VAO) { this.VAO = VAO; }
    public void setVBO(int VBO) { this.VBO = VBO; }
    public void setEBO(int EBO) { this.EBO = EBO; }
    
    // UPDATE SUN'S ROTATION BASED ON TIME
    public void updateRotation(float deltaTime) {
        // CALCULATE ROTATIONAL ANGULAR VELOCITY (RADIANS PER SECOND)
        float rotationalAngularVelocity = (float) (2.0 * Math.PI) / (ROTATION_PERIOD * 24.0f * 3600.0f); // CONVERT DAYS TO SECONDS
        
        // UPDATE ROTATION ANGLE
        rotationAngle += rotationalAngularVelocity * deltaTime;
        
        // KEEP ANGLE IN 0-2π RANGE
        if (rotationAngle > 2.0 * Math.PI) {
            rotationAngle -= (float) (2.0 * Math.PI);
        }
    }
    
    // GETTERS FOR ROTATION DATA
    public float getRotationAngle() { return rotationAngle; }
}
